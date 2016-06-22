package edu.brown.cs.atari_vision.caffe.nnstate;

import burlap.mdp.core.state.State;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by MelRod on 5/27/16.
 */
public interface NNState extends State {

    FloatPointer getInput();
}
