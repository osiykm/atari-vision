package edu.brown.cs.atari_vision.caffe.training;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.mdp.singleagent.SADomain;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainGenerator;
import edu.brown.cs.atari_vision.ale.burlap.ALEEnvironment;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.ale.io.Actions;
import edu.brown.cs.atari_vision.caffe.experiencereplay.FixedSizeMemory;
import edu.brown.cs.atari_vision.caffe.experiencereplay.FrameExperienceMemory;
import edu.brown.cs.atari_vision.caffe.learners.DeepQLearner;
import edu.brown.cs.atari_vision.caffe.experiencereplay.FrameHistoryState;
import edu.brown.cs.atari_vision.caffe.policies.AnnealedEpsilonGreedy;
import edu.brown.cs.atari_vision.caffe.preprocess.DQNPreProcessor;
import edu.brown.cs.atari_vision.caffe.vfa.DQN;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;
import org.bytedeco.javacpp.Loader;

import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by MelRod on 5/31/16.
 */
public class DQNTrainer {

    private static final String ROM = "pong.bin";
    private static final boolean GUI = true;

    public static void main(String[] args) {

        Loader.load(Caffe.class);

        int experienceMemoryLength = 1000000;
        int maxHistoryLength = 4;
        int k = 4;

        ActionSet actionSet = Actions.pongActionSet();

        ALEDomainGenerator domGen = new ALEDomainGenerator(actionSet);
        SADomain domain = domGen.generateDomain();

        FrameExperienceMemory trainingExperienceMemory = new FrameExperienceMemory(experienceMemoryLength, maxHistoryLength, new DQNPreProcessor());
        FrameHistoryState trainingInitialState = new FrameHistoryState(trainingExperienceMemory);
        ALEEnvironment trainingEnv = new ALEEnvironment(domain, trainingInitialState, ROM, k, true, GUI);

        FrameExperienceMemory testExperienceMemory = new FrameExperienceMemory(maxHistoryLength, maxHistoryLength, new DQNPreProcessor());
        FrameHistoryState testInitialState = new FrameHistoryState(testExperienceMemory);
        ALEEnvironment testEnv = new ALEEnvironment(domain, testInitialState, ROM, k, true, GUI);

        double gamma = 0.99;

        DQN dqn = new DQN(actionSet, gamma);
        Policy policy = new AnnealedEpsilonGreedy(dqn, 1.0, 0.1, 1000000);

        DeepQLearner deepQLearner = new DeepQLearner(domain, 0.99, 50000, policy, dqn);
        deepQLearner.setExperienceReplay(trainingExperienceMemory, NNVFA.BATCH_SIZE);

        Policy testPolicy = new EpsilonGreedy(dqn, 0.05);

        // setup helper
        TrainingHelper helper = new TrainingHelper(deepQLearner, dqn, testPolicy, actionSet, trainingEnv, testEnv);
        helper.setTotalTrainingFrames(50000000);
        helper.setTestInterval(50000);
        helper.setNumTestEpisodes(5);
        helper.setNumSampleStates(1000);

        // run helper
        helper.run();
    }
}
