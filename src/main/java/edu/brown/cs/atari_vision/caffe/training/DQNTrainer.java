package edu.brown.cs.atari_vision.caffe.training;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.mdp.singleagent.SADomain;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainGenerator;
import edu.brown.cs.atari_vision.ale.burlap.ALEEnvironment;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.ale.io.Actions;
import edu.brown.cs.atari_vision.caffe.experiencereplay.FixedSizeMemory;
import edu.brown.cs.atari_vision.caffe.learners.DeepQLearner;
import edu.brown.cs.atari_vision.caffe.nnstate.NHistoryState;
import edu.brown.cs.atari_vision.caffe.nnstate.NNState;
import edu.brown.cs.atari_vision.caffe.policies.AnnealedEpsilonGreedy;
import edu.brown.cs.atari_vision.caffe.preprocess.DQNPreProcessor;
import edu.brown.cs.atari_vision.caffe.vfa.DQN;
import org.bytedeco.javacpp.Loader;

/**
 * Created by MelRod on 5/31/16.
 */
public class DQNTrainer {

    private static final String ROM = "pong.bin";
    private static final String SNAPSHOT_FILE_NAME = "dqnPongSnapshot";
    private static final boolean GUI = true;

    public static void main(String[] args) {
        ALEDomainGenerator domGen = new ALEDomainGenerator();
        SADomain domain = domGen.generateDomain();
        NNState initialState = new NHistoryState(4, new DQNPreProcessor());

        int k = 4;
        ALEEnvironment env = new ALEEnvironment(domain, initialState, ROM, k, GUI);

        double gamma = 0.99;
        ActionSet actionSet = Actions.pongActionSet();

        DQN dqn = new DQN(actionSet, gamma);
        Policy policy = new AnnealedEpsilonGreedy(dqn, 1.0, 0.1, 1000000);

        DeepQLearner deepQLearner = new DeepQLearner(domain, 0.99, policy, dqn);
        deepQLearner.setExperienceReplay(new FixedSizeMemory(1000000), 32);

        Policy testPolicy = new EpsilonGreedy(dqn, 0.05);

        // setup helper
        TrainingHelper helper = new TrainingHelper(deepQLearner, dqn, testPolicy, actionSet, env);
        helper.setTotalTrainingFrames(10000000);
        helper.setTestInterval(50000);
        helper.setNumTestEpisodes(10);
        helper.setNumSampleStates(1000);
        helper.setSnapshots(SNAPSHOT_FILE_NAME, 100000);

        // run helper
        helper.run();
    }
}
