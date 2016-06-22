package edu.brown.cs.atari_vision.caffe.vfa;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.mdp.core.Action;
import burlap.mdp.core.SimpleAction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import com.google.common.primitives.Floats;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.caffe.exampledomains.NNGridWorld;
import edu.brown.cs.atari_vision.caffe.preprocess.DQNPreProcessor;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import edu.brown.cs.atari_vision.caffe.visualizers.PongVisualizer;
import org.bytedeco.javacpp.FloatPointer;

import javax.swing.*;

import static org.bytedeco.javacpp.caffe.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelRod on 5/25/16.
 */
public abstract class NNVFA implements ParametricFunction.ParametricStateActionFunction, QProvider {

    // DEBUG
    static JFrame pongVisualizer = PongVisualizer.createPongVisualizer();

    public static final int BATCH_SIZE = 32;

    protected FloatNet caffeNet;
    protected FloatSolver caffeSolver;
    protected FloatMemoryDataLayer inputLayer;
    protected FloatMemoryDataLayer filterLayer;
    protected FloatMemoryDataLayer yLayer;
    protected FloatPointer stateInputs;
    protected FloatPointer primeStateInputs;
    protected FloatPointer dummyInputData;
    protected FloatBlob qValuesBlob;

    protected ActionSet actionSet;
    protected double gamma;

    public NNVFA(ActionSet actionSet, double gamma) {
        this.actionSet = actionSet;
        this.gamma = gamma;
    }

    protected NNVFA(NNVFA vfa) {
        this.actionSet = vfa.actionSet;
        this.gamma = vfa.gamma;
    }

    protected abstract void constructNetwork();

    protected abstract FloatPointer convertStateToInput(State state);

    protected abstract int inputSize();


    public void updateQFunction(List<EnvironmentOutcome> samples, NNVFA staleVfa) {
        int sampleSize = samples.size();
        if (sampleSize < BATCH_SIZE) {
            return;
        }

        // Create input ndArrays

        int inputSize = inputSize();

        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);

            stateInputs.position(i*inputSize).put(convertStateToInput(eo.o).limit(inputSize));

            primeStateInputs.position(i*inputSize).put(convertStateToInput(eo.op).limit(inputSize));
        }

        // Forward pass states

        staleVfa.inputDataIntoLayers(primeStateInputs.position(0), dummyInputData, dummyInputData);
        staleVfa.caffeNet.ForwardPrefilled();

        int numActions = actionSet.size();
        FloatPointer ys = (new FloatPointer(sampleSize * numActions)).zero();
        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);
            float maxQ = blobMax(staleVfa.qValuesBlob, i);

            float y;
            if (eo.terminated) {
                y = (float)eo.r;
            } else {
                y = (float)(eo.r + gamma*maxQ);
            }

            int index = i*numActions + actionSet.map(eo.a.actionName());
            ys.put(index, y);
        }

        FloatPointer actionFilter = new FloatPointer(sampleSize * actionSet.size());
        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);

            int action = actionSet.map(eo.a.actionName());

            int index = i*numActions;
            for (int a = 0; a < numActions; a++) {
                if (a == action) {
                    actionFilter.put(index + a, 1);
                } else {
                    actionFilter.put(index + a, 0);
                }
            }
        }


        // Backprop
        inputDataIntoLayers(stateInputs.position(0), actionFilter, ys);
        caffeSolver.Step(1);
    }

    public float blobMax(FloatBlob blob, int n) {
        float max = Float.NEGATIVE_INFINITY;
        for (int c = 0; c < blob.shape(1); c++) {
            float num = blob.data_at(n, c, 0, 0);
            if (max < num) {
                max = num;
            }
        }
        return max;
    }

    public void updateParamsToMatch(NNVFA vfa) {
//        this.caffeNet.ShareTrainedLayersWith(vfa.caffeNet);
        FloatBlobSharedVector params = this.caffeNet.params();
        FloatBlobSharedVector newParams = vfa.caffeNet.params();
        for (int i = 0; i < params.size(); i++) {
            params.get(i).CopyFrom(newParams.get(i));
        }
    }

    public FloatBlob qValuesForState(State state) {
        int inputSize = inputSize();

        stateInputs.position(0).put(convertStateToInput(state).limit(inputSize));
        inputDataIntoLayers(stateInputs, dummyInputData, dummyInputData);
        caffeNet.ForwardPrefilled();
        return qValuesBlob;
    }

    @Override
    public double evaluate(State state, Action abstractGroundedAction) {
        FloatBlob output = qValuesForState(state);

        int action = actionSet.map(abstractGroundedAction.actionName());
        return output.data_at(0,action,0,0);
    }

    @Override
    public List<QValue> qValues(State state) {

        FloatBlob qValues = qValuesForState(state);
        int numActions = actionSet.size();

        ArrayList<QValue> qValueList = new ArrayList<>(numActions);
        for (int a = 0; a < numActions; a++) {
            QValue q = new QValue(state, new SimpleAction(actionSet.get(a)), qValues.data_at(0, a, 0, 0));
            qValueList.add(q);
        }

        // DEBUG
        PongVisualizer.setQValues(qValueList);

        return qValueList;
    }

    @Override
    public double qValue(State state, Action abstractGroundedAction) {
        return evaluate(state, abstractGroundedAction);
    }

    @Override
    public double value(State s) {
        List<QValue> qs = this.qValues(s);
        double max = Double.NEGATIVE_INFINITY;
        for(QValue q : qs){
            max = Math.max(max, q.q);
        }
        return max;
    }

    protected void inputDataIntoLayers(FloatPointer inputData, FloatPointer filterData, FloatPointer yData) {
        inputLayer.Reset(inputData, dummyInputData, BATCH_SIZE);
        filterLayer.Reset(filterData, dummyInputData, BATCH_SIZE);
        yLayer.Reset(yData, dummyInputData, BATCH_SIZE);
    }

    // Loading
    public void loadWeightsFrom(String fileName) {
        caffeNet.CopyTrainedLayersFrom(fileName);
    }


    // Debug methods
    public static void print2D(FloatPointer ptr, int rows, int cols) {
        print2D(ptr, rows, cols, 1);
    }

    public static void print2D(FloatPointer ptr, int rows, int cols, int n) {

        FloatBuffer buffer = ptr.asBuffer();

        for (int i = 0; i < n; i++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    System.out.print(String.format("%.2f ", buffer.get()));
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void printBlob(FloatBlob blob) {

        for (int n = 0; n < blob.shape(0); n++) {
            for (int c = 0; c < blob.shape(1); c++) {
                if (blob.num_axes() > 3 && blob.shape(2) > 1) {
                    for (int x = 0; x < blob.shape(2); x++) {
                        for (int y = 0; y < blob.shape(3); y++) {
                            System.out.print(blob.data_at(n, c, x, y) + " ");
                        }
                        System.out.println();
                    }
                    System.out.println();
                } else {
                    System.out.print(blob.data_at(n, c, 0, 0) + " ");
//                System.out.print(String.format("%.2f ", blob.data_at(n, c, 0, 0)));
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    protected void printND(FloatPointer ptr, int[] dims) {

        int[] index = new int[dims.length];

        while (true) {
            System.out.print(String.format("%.2f ", ptr.get()));

            int i = 0;
            for (; i < dims.length; i++) {
                index[i]++;
                if (index[i] >= dims[i]) {
                    index[i] = 0;
                    System.out.println();
                } else {
                    break;
                }
            }
            if (i == dims.length) {
                break;
            }
        }
    }


    // Unsupported Operations

    @Override
    public double getParameter(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParameter(int i, double v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int numParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetParameters() {
        throw new UnsupportedOperationException();
    }
}
