package edu.brown.cs.atari_vision.deeprl.nnstate;

import edu.brown.cs.atari_vision.ale.burlap.ALEState;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by MelRod on 5/27/16.
 */
public interface NNState extends ALEState {

    public INDArray getInput();
}
