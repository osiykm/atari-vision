package edu.brown.cs.atari_vision.caffe.preprocess;

import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by MelRod on 5/27/16.
 */
public interface PreProcessor {

    public FloatPointer convertScreenToInput(Mat screen);
    public int outputSize();
}
