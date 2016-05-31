package deeprl.nnstate;

import ale.burlap.ALEState;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by MelRod on 5/27/16.
 */
public interface NNState extends ALEState {

    public INDArray getInput();
}
