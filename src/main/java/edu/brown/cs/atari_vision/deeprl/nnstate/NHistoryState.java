package edu.brown.cs.atari_vision.deeprl.nnstate;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.ALEState;
import edu.brown.cs.atari_vision.ale.screen.ScreenMatrix;
import edu.brown.cs.atari_vision.deeprl.preprocess.PreProcessor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import org.bytedeco.javacpp.opencv_core.*;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.*;

/**
 * Created by MelRod on 5/27/16.
 */
public class NHistoryState implements NNState {

    private INDArray frameHistory;
    private PreProcessor preProcessor;
    private int n; // the history size

    protected NHistoryState(NHistoryState oldState) {
        this.n = oldState.n;
        this.preProcessor = oldState.preProcessor;
    }

    public NHistoryState(int n, PreProcessor preProcessor) {
        this.n = n;
        this.preProcessor = preProcessor;

        int frameSize = preProcessor.outputSize();
        this.frameHistory = Nd4j.zeros(1, frameSize*n);
    }

    @Override
    public INDArray getInput() {
        return frameHistory;
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, Mat newScreen) {

        int frameSize = preProcessor.outputSize();

        // Create new input list
        INDArray newFrameHistory = Nd4j.zeros(frameHistory.shape());

        // Process the new input
        newFrameHistory.put(new INDArrayIndex[]{NDArrayIndex.interval(0, frameSize)}, preProcessor.convertScreenToInput(newScreen));

        // Add history
        newFrameHistory.put(new INDArrayIndex[]{NDArrayIndex.interval(frameSize, n*frameSize)},
                frameHistory.get(new INDArrayIndex[]{NDArrayIndex.interval(0, frameSize*(n-1))}));

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