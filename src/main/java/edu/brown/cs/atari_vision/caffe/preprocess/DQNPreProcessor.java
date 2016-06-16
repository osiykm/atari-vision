package edu.brown.cs.atari_vision.caffe.preprocess;

import org.bytedeco.javacpp.FloatPointer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

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
    public FloatPointer convertScreenToInput(Mat screen) {

        Mat gray = new Mat();
        cvtColor(screen, gray, COLOR_BGR2GRAY);

        Mat downsample = new Mat();
        resize(gray, downsample, new Size(scaleWidth, scaleHeight));

        Mat crop = downsample.apply(new Rect(cropLeft, cropTop, cropWidth, cropHeight));

        Mat floatCrop = new Mat(crop.rows(), crop.cols(), CV_32F);
        crop.convertTo(floatCrop, CV_32F, 1/255.0, 0);

        return new FloatPointer(floatCrop.data());
    }

    @Override
    public int outputSize() {
        return 84*84;
    }
}
