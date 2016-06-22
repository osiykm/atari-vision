package edu.brown.cs.atari_vision.ale.burlap;


import burlap.mdp.core.oo.state.generic.GenericOOState;

import org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by MelRod on 3/18/16.
 */
public class BlankALEState extends GenericOOState {

    public Mat screen;

    public BlankALEState() {}

    public BlankALEState(Mat screen) {
        this.screen = screen;
    }
}
