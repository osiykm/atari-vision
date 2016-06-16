package edu.brown.cs.atari_vision.deeprl.exampledomains;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.common.SinglePFTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.deeprl.experiencereplay.FixedSizeMemory;
import edu.brown.cs.atari_vision.deeprl.learners.DeepQLearner;
import edu.brown.cs.atari_vision.deeprl.policies.AnnealedEpsilonGreedy;
import edu.brown.cs.atari_vision.deeprl.training.TrainingHelper;
import edu.brown.cs.atari_vision.deeprl.vfa.NNVFA;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.stepfunctions.NegativeGradientStepFunction;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by MelRod on 5/29/16.
 */
public class NNGridWorld extends NNVFA {

    static final String SNAPSHOT_FILE_NAME = "nnGridWorldSnapshot";
    static final boolean GUI = false;

    static ActionSet actionSet = new ActionSet(new String[]{
            GridWorldDomain.ACTION_NORTH,
            GridWorldDomain.ACTION_SOUTH,
            GridWorldDomain.ACTION_WEST,
            GridWorldDomain.ACTION_EAST});

    GridWorldDomain gwdg;
    OOSADomain domain;
    RewardFunction rf;
    TerminalFunction tf;
    StateConditionTest goalCondition;
    State initialState;
    HashableStateFactory hashingFactory;
    Environment env;

    public NNGridWorld() {
        super(actionSet, 0.99);

        //create the domain
        gwdg = new GridWorldDomain(11, 11);
        gwdg.setMapToFourRooms();
        rf = new UniformCostRF();
        tf = new SinglePFTF(PropositionalFunction.findPF(gwdg.generatePfs(), GridWorldDomain.PF_AT_LOCATION));
        gwdg.setRf(rf);
        gwdg.setTf(tf);
        domain = gwdg.generateDomain();

        goalCondition = new TFGoalCondition(tf);

        //set up the initial state of the task
        initialState = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, "loc0"));

        //set up the state hashing system for tabular algorithms
        hashingFactory = new SimpleHashableStateFactory();

        //set up the environment for learners algorithms
        env = new SimulatedEnvironment(domain, initialState);

        constructNetwork();
    }

    protected NNGridWorld(NNGridWorld nnGridWorld) {
        super(nnGridWorld);
    }

    @Override
    protected void constructNetwork() {

        int inputSize = gwdg.getWidth() * gwdg.getHeight();
        int outputNum = actionSet.size();
        int iterations = 1;
        int seed = 123;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .regularization(false)
                .learningRate(0.0001)//.biasLearningRate(0.02)
                //.learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.SGD)
                .stepFunction(new NegativeGradientStepFunction())
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputSize)
                        .nOut(256)
                        .activation("relu")
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(256)
                        .nOut(outputNum)
                        .activation("identity")
                        .build())
                .pretrain(false).backprop(true)
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
    }

    @Override
    protected INDArray convertStateToInput(State state) {
        int width = gwdg.getWidth();
        int height = gwdg.getHeight();
        INDArray input = Nd4j.zeros(width * height);

        ObjectInstance agent = ((OOState)state).object(GridWorldDomain.CLASS_AGENT);
        int x = (Integer)agent.get(GridWorldDomain.VAR_X);
        int y = (Integer)agent.get(GridWorldDomain.VAR_Y);

        input.put(0, y*width + x, 1);

        return input;
    }

    @Override
    protected int inputSize() {
        int width = gwdg.getWidth();
        int height = gwdg.getHeight();

        return width * height;
    }

    @Override
    public ParametricFunction copy() {
        return new NNGridWorld(this);
    }


    public static void main(String args[]) {

        NNGridWorld nnGridWorld = new NNGridWorld();

        if (GUI) {
            VisualExplorer exp = new VisualExplorer(nnGridWorld.domain, nnGridWorld.env, GridWorldVisualizer.getVisualizer(nnGridWorld.gwdg.getMap()));
            exp.initGUI();
            exp.startLiveStatePolling(33);
        }


        Policy policy = new AnnealedEpsilonGreedy(nnGridWorld, 1.0, 0.1, 1000000);

        DeepQLearner deepQLearner = new DeepQLearner(nnGridWorld.domain, 0.99, policy, nnGridWorld);
        deepQLearner.setExperienceReplay(new FixedSizeMemory(1000000), 32);

        Policy testPolicy = new EpsilonGreedy(nnGridWorld, 0.05);

        // setup helper
        TrainingHelper helper = new TrainingHelper(deepQLearner, nnGridWorld, testPolicy, actionSet, nnGridWorld.env);
        helper.setTotalTrainingFrames(10000000);
        helper.setTestInterval(100000);
        helper.setNumTestEpisodes(10);
        helper.setNumSampleStates(1000);
        helper.setSnapshots(SNAPSHOT_FILE_NAME, 50000);

        // run helper
        helper.run();
    }
}
