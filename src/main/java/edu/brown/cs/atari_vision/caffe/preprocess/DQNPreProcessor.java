package edu.brown.cs.atari_vision.caffe.preprocess;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by MelRod on 5/25/16.
 */
public class DQNPreProcessor implements PreProcessor {

    static final int scaleWidth = 84;
    static final int scaleHeight = 84;

    public DQNPreProcessor() {

    }

    @Override
    public BytePointer convertScreenToData(Mat screen) {

        Mat gray = new Mat();
        cvtColor(screen, gray, COLOR_BGR2GRAY);

        Mat downsample = new Mat();
        resize(gray, downsample, new Size(scaleWidth, scaleHeight));

        return downsample.data();
    }

    @Override
    public void convertDataToInput(BytePointer data, FloatPointer input, long size) {

        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size is too large to create an opencv Mat");
        }

        int dataSize = outputSize() * (int)size;

        Mat mat = new Mat(1, dataSize, CV_8U, data);
        Mat floatMat = new Mat(1, dataSize, CV_32F, (new BytePointer(input)).position(input.position() * input.sizeof()));

        mat.convertTo(floatMat, CV_32F, 1/255.0, 0);
    }

    @Override
    public int outputSize() {
        return 84*84;
    }
}
