package edu.brown.cs.atari_vision.caffe.vfa;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import com.google.common.primitives.Floats;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.caffe.preprocess.DQNPreProcessor;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import org.bytedeco.javacpp.FloatPointer;

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
public abstract class NNVFA implements ParametricFunction.ParametricStateActionFunction, QFunction {

    protected FloatNet caffeNet;
    protected FloatMemoryDataLayer inputLayer;
    protected FloatMemoryDataLayer filterLayer;
    protected FloatMemoryDataLayer yLayer;
    protected FloatPointer dummyInputData;

    protected PreProcessor preProcessor;

    protected ActionSet actionSet;
    protected double gamma;

    protected FloatBlobVector stateInputs;
    protected FloatBlobVector primeStateInputs;

    public NNVFA(ActionSet actionSet, double gamma) {
        this.actionSet = actionSet;
        this.gamma = gamma;

        preProcessor = new DQNPreProcessor();
    }

    protected NNVFA(NNVFA vfa) {
        this.actionSet = vfa.actionSet;
        this.gamma = vfa.gamma;
        this.preProcessor = vfa.preProcessor;
        this.caffeNet = vfa.caffeNet;
    }

    protected abstract void constructNetwork();

    protected abstract FloatBlob convertStateToInput(State state);

    protected abstract int inputSize();


    public void updateQFunction(List<EnvironmentOutcome> samples, NNVFA staleVfa) {
        int sampleSize = samples.size();

        // Create input ndArrays

        int inputSize = inputSize();

        if (stateInputs == null || stateInputs.size() != inputSize) {
            primeStateInputs = new FloatBlobVector(inputSize);
            stateInputs = new FloatBlobVector(inputSize);

            dummyInputData = new FloatPointer(sampleSize * inputSize);
        }

        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);

            stateInputs.put(i, convertStateToInput(eo.o));
            primeStateInputs.put(i, convertStateToInput(eo.op));
        }

        // Forward pass states

        staleVfa.inputLayer.Reset(new FloatPointer(primeStateInputs), dummyInputData, inputSize);
        FloatBlobVector primeQValues = staleVfa.caffeNet.ForwardPrefilled();

        FloatPointer ys = new FloatPointer(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);
            float maxQ = blobMax(new FloatPointer(primeQValues.get(i)));

            float y;
            if (eo.terminated) {
                y = (float)eo.r;
            } else {
                y = (float)(eo.r + gamma*maxQ);
            }

            ys.put(y);
        }

        FloatPointer actionFilter = new FloatPointer(sampleSize * actionSet.size());
        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);

            int action = actionSet.map(eo.a.actionName());
            for (int a = 0; a < actionSet.size(); a++) {
                if (a == action) {
                    actionFilter.put(1);
                } else {
                    actionFilter.put(0);
                }
            }
        }


        // Backprop
        inputLayer.Reset(new FloatPointer(stateInputs), dummyInputData, inputSize);
        filterLayer.Reset(actionFilter, dummyInputData, inputSize);
        yLayer.Reset(ys, dummyInputData, inputSize);
        caffeNet.ClearParamDiffs();
        caffeNet.ForwardPrefilled();
        caffeNet.Backward();
        caffeNet.Update();
    }

    public float blobMax(FloatPointer blob) {

        FloatBuffer buffer = blob.asBuffer();

        if (!buffer.hasRemaining()) {
            return 0;
        }

        float max = buffer.get();
        while (buffer.hasRemaining()) {
            float num = buffer.get();
            if (max < num) {
                max = num;
            }
        }
        return max;
    }

    public void updateParamsToMatch(NNVFA vfa) {
        this.caffeNet.ShareTrainedLayersWith(vfa.caffeNet);
    }

    public FloatBlobVector qValuesForState(State state) {
        FloatBlob input = convertStateToInput(state);

        inputLayer.Reset(new FloatPointer(input), dummyInputData, 1);
        FloatBlobVector output = caffeNet.ForwardPrefilled();

        return output;
    }

    @Override
    public double evaluate(State state, Action abstractGroundedAction) {
        FloatBlobVector output = qValuesForState(state);

        int action = actionSet.map(abstractGroundedAction.actionName());
        return output.get(action).data_at(0,0,0,0);
    }

    @Override
    public List<QValue> getQs(State state) {

        FloatBlobVector qVector = qValuesForState(state);
        ArrayList<QValue> qValues = new ArrayList<>((int)qVector.size());
        for (int a = 0; a < qVector.size(); a++) {
            QValue q = new QValue(state, new SimpleAction(actionSet.get(a)), qVector.get(a).data_at(0,0,0,0));
            qValues.add(q);
        }
        return qValues;
    }

    @Override
    public QValue getQ(State state, Action abstractGroundedAction) {
        double q = evaluate(state, abstractGroundedAction);
        return new QValue(state, abstractGroundedAction, q);
    }

    @Override
    public double value(State s) {
        List<QValue> qs = this.getQs(s);
        double max = Double.NEGATIVE_INFINITY;
        for(QValue q : qs){
            max = Math.max(max, q.q);
        }
        return max;
    }


    // Saving
    public void saveWeightsTo(String fileName) {
        // TODO: implement
//        //Write the network parameters:
//        try(DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(fileName+".bin")))){
//            Nd4j.write(model.params(),dos);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //Save the updater:
//        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName+".updater.bin"))){
//            oos.writeObject(model.getUpdater());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    // Loading
    public void setWeightsFrom(String fileName) {
        // TODO: implement
//        //Load parameters from disk:
//        try(DataInputStream dis = new DataInputStream(new FileInputStream(fileName+".bin"))){
//
//            INDArray newParams = Nd4j.read(dis);
//            this.model.setParams(newParams);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //Load the updater:
//        Updater updater;
//        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName+".updater.bin"))){
//            updater = (Updater) ois.readObject();
//            model.setUpdater(updater);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
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
