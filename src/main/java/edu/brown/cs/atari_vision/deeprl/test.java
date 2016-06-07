package edu.brown.cs.atari_vision.deeprl;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.RandomPolicy;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainGenerator;
import edu.brown.cs.atari_vision.ale.burlap.ALEEnvironment;
import edu.brown.cs.atari_vision.deeprl.nnstate.NHistoryState;
import edu.brown.cs.atari_vision.deeprl.nnstate.NNState;
import edu.brown.cs.atari_vision.deeprl.policies.AnnealedEpsilonGreedy;
import edu.brown.cs.atari_vision.deeprl.preprocess.DQNPreProcessor;
import edu.brown.cs.atari_vision.deeprl.training.TrainingHelper;
import edu.brown.cs.atari_vision.deeprl.vfa.DQN;
import org.nd4j.linalg.api.ndarray.INDArray;

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
        SADomain domain = domGen.generateDomain();
        NNState initialState = new NHistoryState(4, new DQNPreProcessor());

        Policy policy = new RandomPolicy(domain);
        ALEEnvironment env = new ALEEnvironment(domain, initialState, rom, true);

        runFrames(100, policy, env);
    }

    static void runFrames(int frames, Policy policy, ALEEnvironment env) {
        for (int i = 0; i < frames; i++) {
            State curState = env.currentObservation();
            Action action = policy.action(curState);

            env.executeAction(action);
        }

        INDArray input = ((NNState)env.currentObservation()).getInput();
        saveInputAsImage(input);

        env.die();
    }

    static void saveInputAsImage(INDArray input) {
        int rows = 84;
        int cols = 84;

        BufferedImage img = new BufferedImage(rows, cols, BufferedImage.TYPE_INT_RGB);

        // Map each pixel
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                int gray = input.getInt(0, (y*cols) + x);
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
