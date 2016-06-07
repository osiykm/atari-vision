package edu.brown.cs.atari_vision.deeprl.vfa;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.deeprl.nnstate.NNState;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.conf.stepfunctions.NegativeGradientStepFunction;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by MelRod on 5/24/16.
 */
public class DQN extends NNVFA {

    public DQN(ActionSet actionSet, double gamma) {
        super(actionSet, gamma);
        constructNetwork();
    }

    protected DQN(DQN dqn) {
        super(dqn);
    }

    @Override
    protected void constructNetwork() {

        int nFrameMemory = 4;
        int outputNum = actionSet.size();
        int iterations = 1;
        int seed = 123;

        MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .regularization(true).l2(0.0005)
                .learningRate(0.01)//.biasLearningRate(0.02)
                //.learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .stepFunction(new NegativeGradientStepFunction())
                .list()
                .layer(0, new ConvolutionLayer.Builder(8, 8)
                        .nIn(nFrameMemory)
                        .stride(4, 4)
                        .nOut(16)
                        .activation("relu")
                        .build())
                .layer(1, new ConvolutionLayer.Builder(4, 4)
                        .nIn(nFrameMemory)
                        .stride(2, 2)
                        .nOut(32)
                        .activation("relu")
                        .build())
                .layer(2, new DenseLayer.Builder()
                        .activation("relu")
                        .nOut(256)
                        .build())
                .layer(3, new DenseLayer.Builder()
                        .nOut(outputNum)
                        .activation("identity")
                        .build())
                .backprop(true).pretrain(false);
        new ConvolutionLayerSetup(builder,84,84,nFrameMemory);

        MultiLayerConfiguration conf = builder.build();
        model = new MultiLayerNetwork(conf);
        model.init();
    }

    @Override
    protected INDArray convertStateToInput(State state) {
        return ((NNState)state).getInput();
    }

    @Override
    protected int inputSize() {
        return 84*84*4;
    }

    @Override
    public ParametricFunction.ParametricStateActionFunction copy() {
        return new DQN(this);
    }
}
