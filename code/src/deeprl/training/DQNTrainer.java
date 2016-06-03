package deeprl.training;

import ale.burlap.ALEDomainGenerator;
import ale.burlap.ALEEnvironment;
import ale.burlap.ALEKSkipEnvironment;
import ale.io.ActionSet;
import ale.io.Actions;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.oomdp.core.Domain;
import deeprl.experiencereplay.FixedSizeMemory;
import deeprl.learners.DeepQLearner;
import deeprl.nnstate.NHistoryState;
import deeprl.nnstate.NNState;
import deeprl.policies.AnnealedEpsilonGreedy;
import deeprl.preprocess.DQNPreProcessor;
import deeprl.vfa.DQN;

import java.io.File;

/**
 * Created by MelRod on 5/31/16.
 */
public class DQNTrainer {

    private static final String ROM = "pong.bin";
    private static final String SNAPSHOT_FILE_NAME = "dqnPongSnapshot";
    private static final boolean GUI = true;

    public static void main(String[] args) {
        ALEDomainGenerator domGen = new ALEDomainGenerator();
        Domain domain = domGen.generateDomain();
        NNState initialState = new NHistoryState(4, new DQNPreProcessor());

        int k = 4;
        ALEEnvironment env = new ALEKSkipEnvironment(domain, initialState, k, ROM, GUI);

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
