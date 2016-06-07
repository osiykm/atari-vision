package edu.brown.cs.atari_vision.deeprl.preprocess;

import edu.brown.cs.atari_vision.ale.burlap.ALEDomainConstants;
import edu.brown.cs.atari_vision.ale.screen.ColorPalette;
import edu.brown.cs.atari_vision.ale.screen.NTSCPalette;
import org.canova.image.loader.NativeImageLoader;
import org.canova.image.transform.CropImageTransform;
import org.canova.image.transform.MultiImageTransform;
import org.canova.image.transform.ScaleImageTransform;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import org.bytedeco.javacpp.opencv_core.*;

import java.awt.*;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * Created by MelRod on 5/25/16.
 */
public class DQNPreProcessor implements PreProcessor {

    static final int initialWidth = ALEDomainConstants.ALEScreenHeight;
    static final int initialHeight = ALEDomainConstants.ALEScreenHeight;

    static final int scaleWidth = 84;
    static final int scaleHeight = 110;

    static final int cropTop = 20;
    static final int cropLeft = 0;
    static final int cropWidth = 84;
    static final int cropHeight = 84;

    NativeImageLoader imageLoader;

    public DQNPreProcessor() {
        imageLoader = new NativeImageLoader();
    }

    @Override
    public INDArray convertScreenToInput(Mat screen) {

        Mat gray = new Mat();
        cvtColor(screen, gray, COLOR_BGR2GRAY);

        Mat downsample = new Mat();
        resize(gray, downsample, new Size(scaleWidth, scaleHeight));

        Mat crop = downsample.apply(new Rect(cropLeft, cropTop, cropWidth, cropHeight));

        try {
            INDArray array = imageLoader.asMatrix(crop);
            return array.reshape(1, array.length());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int outputSize() {
        return 84*84;
    }

}
