package deeprl.exampledomains;

import ale.io.ActionSet;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.auxiliary.common.SinglePFTF;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import deeprl.training.TrainingHelper;
import deeprl.experiencereplay.FixedSizeMemory;
import deeprl.learners.DeepQLearner;
import deeprl.policies.AnnealedEpsilonGreedy;
import deeprl.vfa.NNVFA;
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

    static ActionSet actionSet = new ActionSet(new String[]{
            GridWorldDomain.ACTIONNORTH,
            GridWorldDomain.ACTIONSOUTH,
            GridWorldDomain.ACTIONWEST,
            GridWorldDomain.ACTIONEAST});

    GridWorldDomain gwdg;
    Domain domain;
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
//        int[][] map = new int[5][5];
//        gwdg.setMap(map);
        gwdg.setMapToFourRooms();
        domain = gwdg.generateDomain();

        //define the task
        rf = new UniformCostRF();
        tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION));
        goalCondition = new TFGoalCondition(tf);

        //set up the initial state of the task
        initialState = GridWorldDomain.getOneAgentNLocationState(domain, 1);
        GridWorldDomain.setAgent(initialState, 0, 0);
        GridWorldDomain.setLocation(initialState, 0, 10, 10);

        //set up the state hashing system for tabular algorithms
        hashingFactory = new SimpleHashableStateFactory();

        //set up the environment for learners algorithms
        env = new SimulatedEnvironment(domain, rf, tf, initialState);

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

        ObjectInstance agent = state.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0);
        int x = agent.getIntValForAttribute(GridWorldDomain.ATTX);
        int y = agent.getIntValForAttribute(GridWorldDomain.ATTY);

        input.put(0, y*width + x, 1);

        return input;
    }

    @Override
    public ParametricFunction copy() {
        return new NNGridWorld(this);
    }


    public static void main(String args[]) {

        NNGridWorld nnGridWorld = new NNGridWorld();

        VisualExplorer exp = new VisualExplorer(nnGridWorld.domain, nnGridWorld.env, GridWorldVisualizer.getVisualizer(nnGridWorld.gwdg.getMap()));
        exp.initGUI();
        exp.startLiveStatePolling(10);


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

        // run helper
        helper.run();
    }
}
