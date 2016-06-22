package edu.brown.cs.atari_vision.caffe.training;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainGenerator;
import edu.brown.cs.atari_vision.ale.burlap.ALEEnvironment;
import edu.brown.cs.atari_vision.ale.burlap.ALEStateGenerator;
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
public class DQNTrainer extends TrainingHelper {

    static final String ROM = "pong.bin";
    static final boolean GUI = true;

    static final int experienceMemoryLength = 1000000;
    static int maxHistoryLength = 4;
    static int frameSkip = 4;

    static final double epsilonStart = 1;
    static final double epsilonEnd = 0.1;
    static final int epsilonAnnealDuration = 1000000;

    static final double gamma = 0.99;



    protected ALEStateGenerator<FrameHistoryState> trainingStateGenerator;
    protected ALEStateGenerator<FrameHistoryState> testStateGenerator;

    public DQNTrainer(DeepQLearner learner, NNVFA vfa, Policy testPolicy, ActionSet actionSet, Environment env,
                      ALEStateGenerator<FrameHistoryState> trainingStateGenerator,
                      ALEStateGenerator<FrameHistoryState> testStateGenerator) {
        super(learner, vfa, testPolicy, actionSet, env);

        this.trainingStateGenerator = trainingStateGenerator;
        this.testStateGenerator = testStateGenerator;
    }

    @Override
    public void prepareForTraining() {
        ((ALEEnvironment<FrameHistoryState>)this.env).setStateGenerator(trainingStateGenerator);
    }

    @Override
    public void prepareForTesting() {
        ((ALEEnvironment<FrameHistoryState>)this.env).setStateGenerator(trainingStateGenerator);
    }

    public static void main(String[] args) {

        Loader.load(Caffe.class);



        ActionSet actionSet = Actions.pongActionSet();

        ALEDomainGenerator domGen = new ALEDomainGenerator(actionSet);
        SADomain domain = domGen.generateDomain();

        FrameExperienceMemory trainingExperienceMemory = new FrameExperienceMemory(experienceMemoryLength, maxHistoryLength, new DQNPreProcessor());
        ALEEnvironment env = new ALEEnvironment(domain, trainingExperienceMemory, ROM, frameSkip, GUI);

        FrameExperienceMemory testExperienceMemory = new FrameExperienceMemory(maxHistoryLength, maxHistoryLength, new DQNPreProcessor());

        DQN dqn = new DQN(actionSet, gamma);
        Policy policy = new AnnealedEpsilonGreedy(dqn, epsilonStart, epsilonEnd, epsilonAnnealDuration);

        // DEBUG: should be 50,000
        DeepQLearner deepQLearner = new DeepQLearner(domain, gamma, 500, policy, dqn);
        deepQLearner.setExperienceReplay(trainingExperienceMemory, NNVFA.BATCH_SIZE);

        Policy testPolicy = new EpsilonGreedy(dqn, 0.05);

        // setup helper
        TrainingHelper helper = new DQNTrainer(deepQLearner, dqn, testPolicy, actionSet, env, trainingExperienceMemory, testExperienceMemory);
        helper.setTotalTrainingFrames(50000000);
        helper.setTestInterval(50000);
        helper.setNumTestEpisodes(5);
        helper.setNumSampleStates(1000);

        // run helper
        helper.run();
    }
}
