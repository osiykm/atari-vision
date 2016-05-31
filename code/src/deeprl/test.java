package deeprl;

import ale.burlap.*;
import ale.io.ActionSet;
import ale.io.Actions;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import deeprl.experiencereplay.FixedSizeMemory;
import deeprl.learners.DeepQLearner;
import deeprl.nnstate.NNState;
import deeprl.nnstate.NHistoryState;
import deeprl.policies.AnnealedEpsilonGreedy;
import deeprl.preprocess.DQNPreProcessor;
import deeprl.training.TrainingHelper;
import deeprl.vfa.DQN;
import org.nd4j.linalg.api.ndarray.INDArray;
import burlap.oomdp.core.Domain;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by MelRod on 5/23/16.
 */
public class test {

    private static final String rom = "pong.bin";

    private static DQNPreProcessor preProcessor = new DQNPreProcessor();

    public static void main(String[] args) {
        ALEDomainGenerator domGen = new ALEDomainGenerator();
        Domain domain = domGen.generateDomain();
        NNState initialState = new NHistoryState(4, new DQNPreProcessor());

        int k = 4;
        ALEEnvironment env = new ALEKSkipEnvironment(domain, initialState, k, rom, true);

        double gamma = 0.99;
        ActionSet actionSet = Actions.pongActionSet();

        DQN dqn = new DQN(actionSet, gamma);
        Policy policy = new AnnealedEpsilonGreedy(dqn, 1.0, 0.1, 1000000);

        DeepQLearner deepQLearner = new DeepQLearner(domain, 0.99, policy, dqn);
//        deepQLearner.setExperienceReplay(new FixedSizeMemory(1000000), 32);
        deepQLearner.setExperienceReplay(new FixedSizeMemory(100000), 32);

        Policy testPolicy = new EpsilonGreedy(dqn, 0.05);

        // setup helper
        TrainingHelper helper = new TrainingHelper(deepQLearner, dqn, testPolicy, env);
        helper.setTotalTrainingFrames(10000000);
        helper.setTestInterval(100000);
        helper.setNumTestEpisodes(10);
        helper.setSnapshots("dqnPongSnapshot", 100000);

        // run helper
        helper.run();
    }

    static void runFrames(int frames, Policy policy, ALEEnvironment env) {
        for (int i = 0; i < frames; i++) {
            State curState = env.getCurrentObservation();
            GroundedAction action = (GroundedAction)policy.getAction(curState);

            action.executeIn(env);
        }

        INDArray input = ((NNState)env.getCurrentObservation()).getInput();
        saveInputAsImage(input);

        env.die();
    }

    static void saveInputAsImage(INDArray input) {
        BufferedImage img = new BufferedImage(input.columns(), input.rows(), BufferedImage.TYPE_INT_RGB);

        // Map each pixel
        for (int x = 0; x < input.columns(); x++) {
            for (int y = 0; y < input.rows(); y++) {
                int gray = input.getInt(y, x);
                Color c = new Color(gray, gray, gray);
                img.setRGB(x, y, c.getRGB());
            }
        }

        File outputfile = new File("screen.png");
        try {
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
