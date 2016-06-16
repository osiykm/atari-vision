package edu.brown.cs.atari_vision.caffe.nnstate;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.ALEState;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;
import static org.bytedeco.javacpp.opencv_core.*;

import java.util.List;

/**
 * Created by MelRod on 5/27/16.
 */
public class NHistoryState implements NNState {

    private FloatPointer frameHistory;
    private PreProcessor preProcessor;
    private int n; // the history size

    protected NHistoryState(NHistoryState oldState) {
        this.n = oldState.n;
        this.preProcessor = oldState.preProcessor;
    }

    public NHistoryState(int n, PreProcessor preProcessor) {
        this.n = n;
        this.preProcessor = preProcessor;

        int outputSize = preProcessor.outputSize();
        this.frameHistory = (new FloatPointer(n * outputSize)).fill(0);
    }

    @Override
    public FloatPointer getInput() {
        return frameHistory;
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, Mat newScreen) {

        int outputSize = preProcessor.outputSize();

        // Create new input list
        FloatPointer newFrameHistory = new FloatPointer(n * outputSize);

        // Process the new input
        FloatPointer newInput = preProcessor.convertScreenToInput(newScreen).limit(outputSize);
        newFrameHistory.position(0).put(newInput);

        // Add history
        newFrameHistory.position(outputSize).put(frameHistory.position(0).limit((n - 1)*outputSize));

        // Create new state
        NHistoryState newState = new NHistoryState(this);
        newState.frameHistory = newFrameHistory;

        return newState;
    }

    @Override
    public List<Object> variableKeys() {
        return null;
    }

    @Override
    public Object get(Object variableKey) {
        return null;
    }

    @Override
    public State copy() {
        NHistoryState newState = new NHistoryState(this);
        newState.frameHistory = frameHistory;
        return newState;
    }
}