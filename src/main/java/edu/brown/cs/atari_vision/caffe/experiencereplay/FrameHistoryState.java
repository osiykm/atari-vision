package edu.brown.cs.atari_vision.caffe.experiencereplay;

import burlap.mdp.core.state.State;

import java.util.List;

/**
 * Created by MelRod on 5/27/16.
 */
public class FrameHistoryState implements State {

    public long index;
    public int historyLength;
    public FrameExperienceMemory memory;


    FrameHistoryState(FrameExperienceMemory memory, long index, int historyLength) {
        this.memory = memory;

        this.index = index;
        this.historyLength = historyLength;
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