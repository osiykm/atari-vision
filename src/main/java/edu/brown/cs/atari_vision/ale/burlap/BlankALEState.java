package edu.brown.cs.atari_vision.ale.burlap;

import burlap.mdp.core.Domain;
import org.bytedeco.javacpp.opencv_core.*;

import java.util.*;
import java.util.Arrays;

/**
 * Created by MelRod on 3/18/16.
 */
public class BlankALEState implements ALEState {

    private Mat screen;

    public BlankALEState() {}
    public BlankALEState(Mat screen) {
        this.screen = screen;
    }
    public BlankALEState(BlankALEState state) {
        this.screen = state.screen.clone();
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, Mat newScreen) {
        return new BlankALEState(newScreen);
    }

    public Mat getScreen() {
        return screen;
    }

    @Override
    public List<Object> variableKeys() {
        return Arrays.asList(new Object[]{(Integer)0});
    }

    @Override
    public Object get(Object variableKey) {
        return getScreen();
    }

    @Override
    public ALEState copy() {
        return new BlankALEState(this);
    }
}
