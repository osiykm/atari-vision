package edu.brown.cs.atari_vision.caffe.experiencereplay;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.caffe.nnstate.NNState;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.opencv_core.*;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by MelRod on 5/27/16.
 */
public class FrameHistoryState implements NNState {

    public long index;
    public int historyLength;
    public FrameExperienceMemory memory;


    FrameHistoryState(FrameExperienceMemory memory, long index, int historyLength) {
        this.memory = memory;

        this.index = index;
        this.historyLength = historyLength;
    }

    @Override
    public void getInput(FloatPointer input) {
        memory.getStateInput(this, input);
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
        FrameHistoryState newState = new FrameHistoryState(this.memory, this.index, this.historyLength);
        return newState;
    }
}