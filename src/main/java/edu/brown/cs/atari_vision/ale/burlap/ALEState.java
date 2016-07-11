package edu.brown.cs.atari_vision.ale.burlap;

import burlap.mdp.core.state.State;
import org.bytedeco.javacpp.opencv_core.*;

import java.util.List;

/**
 * Created by MelRod on 3/18/16.
 */
public class ALEState implements State {

    protected Mat screen;

    public ALEState(Mat screen) {
        this.screen = screen;
    }

    public Mat getScreen() {
        return screen;
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
        return null;
    }
}
