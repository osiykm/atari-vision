package edu.brown.cs.atari_vision.caffe.preprocess;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by MelRod on 5/25/16.
 */
public class DQNPreProcessor implements PreProcessor {

    static final int scaleWidth = 84;
    static final int scaleHeight = 110;

    static final int cropTop = 20;
    static final int cropLeft = 0;
    static final int cropWidth = 84;
    static final int cropHeight = 84;

    public DQNPreProcessor() {

    }

    @Override
    public FloatBlob convertScreenToInput(Mat screen) {

        Mat gray = new Mat();
        cvtColor(screen, gray, COLOR_BGR2GRAY);

        Mat downsample = new Mat();
        resize(gray, downsample, new Size(scaleWidth, scaleHeight));

        Mat crop = downsample.apply(new Rect(cropLeft, cropTop, cropWidth, cropHeight));

        return new FloatBlob(crop.data());
    }

    @Override
    public int outputSize() {
        return 84*84;
    }

}
