package edu.brown.cs.atari_vision.caffe;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.RandomPolicy;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainGenerator;
import edu.brown.cs.atari_vision.ale.burlap.ALEEnvironment;
import edu.brown.cs.atari_vision.caffe.nnstate.NHistoryState;
import edu.brown.cs.atari_vision.caffe.nnstate.NNState;
import edu.brown.cs.atari_vision.caffe.preprocess.DQNPreProcessor;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.opencv_core.*;

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
        int experienceMemory = 10000;

        ALEDomainGenerator domGen = new ALEDomainGenerator();
        SADomain domain = domGen.generateDomain();
        NHistoryState initialState = new NHistoryState(4, experienceMemory, new DQNPreProcessor());

        Policy policy = new RandomPolicy(domain);
        ALEEnvironment env = new ALEEnvironment(domain, initialState, rom, true, true);

        runFrames(100, policy, env);
    }

    static void runFrames(int frames, Policy policy, ALEEnvironment env) {
        for (int i = 0; i < frames; i++) {
            State curState = env.currentObservation();
            Action action = policy.action(curState);

            env.executeAction(action);
        }

        FloatPointer input = ((NNState)env.currentObservation()).getInput();

        env.die();
    }
}
