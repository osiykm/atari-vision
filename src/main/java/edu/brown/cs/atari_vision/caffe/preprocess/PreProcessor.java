package edu.brown.cs.atari_vision.caffe.preprocess;

import static org.bytedeco.javacpp.caffe.*;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by MelRod on 5/27/16.
 */
public interface PreProcessor {

    public FloatBlob convertScreenToInput(Mat screen);
    public int outputSize();
}
