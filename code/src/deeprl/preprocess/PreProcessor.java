package deeprl.preprocess;

import ale.screen.ScreenMatrix;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by MelRod on 5/27/16.
 */
public interface PreProcessor {

    public INDArray convertScreenToInput(ScreenMatrix screen);
    public int inputSize();
}
