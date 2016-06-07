package edu.brown.cs.atari_vision.deeprl.vfa;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.Action;
import burlap.mdp.core.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.deeprl.preprocess.DQNPreProcessor;
import edu.brown.cs.atari_vision.deeprl.preprocess.PreProcessor;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.updater.UpdaterCreator;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.stepfunctions.NegativeGradientStepFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelRod on 5/25/16.
 */
public abstract class NNVFA implements ParametricFunction.ParametricStateActionFunction, QFunction {

    protected MultiLayerNetwork model;
    protected StepFunction stepFunction;
    protected PreProcessor preProcessor;
    protected Updater updater;

    protected ActionSet actionSet;
    protected double gamma;

    protected INDArray stateInputs;
    protected INDArray primeStateInputs;
    protected INDArray epsilon;

    public NNVFA(ActionSet actionSet, double gamma) {
        this.actionSet = actionSet;
        this.gamma = gamma;

        preProcessor = new DQNPreProcessor();

        stepFunction = new NegativeGradientStepFunction();
    }

    protected NNVFA(NNVFA vfa) {
        this.actionSet = vfa.actionSet;
        this.gamma = vfa.gamma;
        this.preProcessor = vfa.preProcessor;
        this.stepFunction = vfa.stepFunction;

        // deep copy model
        this.model = vfa.model.clone();
    }

    protected abstract void constructNetwork();

    protected abstract INDArray convertStateToInput(State state);

    protected abstract int inputSize();


    public void updateQFunction(List<EnvironmentOutcome> samples, NNVFA staleVfa) {
        int sampleSize = samples.size();

        // Create input ndArrays

        int inputSize = inputSize();

        if (stateInputs == null || stateInputs.rows() != inputSize) {
            primeStateInputs = Nd4j.zeros(sampleSize, inputSize);
            stateInputs = Nd4j.zeros(sampleSize, inputSize);
            epsilon = Nd4j.zeros(sampleSize, actionSet.size());
        }

        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);

            stateInputs.putRow(i, convertStateToInput(eo.o));
            primeStateInputs.putRow(i, convertStateToInput(eo.op));
        }

        // Forward pass states

        INDArray primeQValues = staleVfa.model.output(primeStateInputs);
        INDArray maxPrimeQValues = Nd4j.max(primeQValues, 1);
        // must be called second in case this==staleVfa, since model.output() sets the model input for backprop
        // since output() sets the model.input
        INDArray qValues = this.model.output(stateInputs);


        // Create errors to backprop

        for (int i = 0; i < sampleSize; i++) {
            EnvironmentOutcome eo = samples.get(i);

            int action = actionSet.map(eo.a.actionName());
            double y;
            if (eo.terminated) {
                y = eo.r;
            } else {
                y = eo.r + gamma*maxPrimeQValues.getDouble(i);
            }
            double error = -(y - qValues.getDouble(i, action));

            epsilon.putScalar(new int[]{i, action}, error);
        }

        // Backprop errors for actions

        minibatchStep(epsilon);
    }

    public void minibatchStep(INDArray epsilon) {
        // TODO: allow mini batch =/= sample size

        Pair<Gradient,INDArray> pair = model.backpropGradient(epsilon);
        Gradient gradient = pair.getFirst();

//        if (updater == null)
//            updater = UpdaterCreator.getUpdater(model);
//        updater.update(model, gradient, 0, epsilon.rows());
//
//        INDArray params = model.params();
//        stepFunction.step(params,gradient.gradient());
//        model.setParams(params);    //params() may not be in-place
//
//        // update iteration listeners
//        for(IterationListener listener : model.getListeners())
//            listener.iterationDone(model, 0);

        INDArray params = model.params();
        model.setParams(params.addi(gradient.gradient().muli(0.05)));
    }

    public void updateParamsToMatch(NNVFA vfa) {
        this.model.setParams(vfa.model.params());
    }

    public INDArray qValuesForState(State state) {
        INDArray input = convertStateToInput(state);
        INDArray output = model.output(input);

        return output;
    }

    @Override
    public double evaluate(State state, Action abstractGroundedAction) {
        INDArray output = qValuesForState(state);

        int action = actionSet.map(abstractGroundedAction.actionName());
        return output.getDouble(action);
    }

    public List<QValue> getQs(State state) {
        INDArray qArray = qValuesForState(state);
        ArrayList<QValue> qValues = new ArrayList<>(qArray.size(0));
        for (int a = 0; a < qArray.size(1); a++) {
            QValue q = new QValue(state, new SimpleAction(actionSet.get(a)), qArray.getDouble(0, a));
            qValues.add(q);
        }
        return qValues;
    }

    public QValue getQ(State state, Action abstractGroundedAction) {
        double q = evaluate(state, abstractGroundedAction);
        return new QValue(state, abstractGroundedAction, q);
    }

    public double value(State state) {
        INDArray qVals = qValuesForState(state);

        return qVals.maxNumber().doubleValue();
    }

    @Override
    public int numParameters() {
        return model.numParams();
    }

    @Override
    public void resetParameters() {
        model.clear();
    }


    // Saving
    public void saveWeightsTo(String fileName) {
        //Write the network parameters:
        try(DataOutputStream dos = new DataOutputStream(Files.newOutputStream(Paths.get(fileName+".bin")))){
            Nd4j.write(model.params(),dos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Save the updater:
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName+".updater.bin"))){
            oos.writeObject(model.getUpdater());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loading
    public void setWeightsFrom(String fileName) {
        //Load parameters from disk:
        try(DataInputStream dis = new DataInputStream(new FileInputStream(fileName+".bin"))){

            INDArray newParams = Nd4j.read(dis);
            this.model.setParams(newParams);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Load the updater:
        org.deeplearning4j.nn.api.Updater updater;
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName+".updater.bin"))){
            updater = (org.deeplearning4j.nn.api.Updater) ois.readObject();
            model.setUpdater(updater);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
}
