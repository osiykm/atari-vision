package edu.brown.cs.atari_vision.deeprl.preprocess;

import org.bytedeco.javacpp.opencv_core;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by MelRod on 5/27/16.
 */
public interface PreProcessor {

    public INDArray convertScreenToInput(opencv_core.Mat screen);
    public int outputSize();
}
