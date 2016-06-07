package edu.brown.cs.atari_vision.spaceinvaders.cv;

import edu.brown.cs.atari_vision.ale.screen.ScreenConverter;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 30/3/16.
 */
public class ContourFinder {
    /** Object for converting ScreenMatrix to OpenCV Mat. */
    private ScreenConverter screenConverter;

    private final boolean DRAW_RECTS = false;

    // Parameters
    private final double contourThresh = 8;
    private final double conturMax = 100.0;
    private final int threshSize = 3;
    private final double threshBias = 1.0;

    public ContourFinder() {
        screenConverter = new ScreenConverter();
    }

    public List<Point> findSprites(Mat screen) {
        ArrayList<Point> sprites = new ArrayList<>();

        // Convert screen to Mat
        Mat grey = new Mat();
        Mat filtered = new Mat();
        Mat mask = new Mat();
        Mat result = new Mat();
        Mat gradX = new Mat();
        Mat gradY = new Mat();

        // Filter out ground
        // TODO: convert this code
//        cvInRange(screen, new Scalar(18, 80, 75), new Scalar(25, 90, 85), mask);
        //Imgcodecs.imwrite("mask.png", mask);
        bitwise_not(mask, mask);
        screen.copyTo(filtered, mask);

        // Convert to grey
        cvtColor(filtered, grey, COLOR_BGR2GRAY);
        //Imgcodecs.imwrite("bw.png", grey);

        // Perform thresholding
        adaptiveThreshold(grey, result, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY,
                threshSize, threshBias);
        //Imgcodecs.imwrite("thresh.png", result);

        // Find contours
        MatVector contours = new MatVector();
        findContours(result, contours, new Mat(), RETR_LIST, CHAIN_APPROX_TC89_KCOS);
        //Imgcodecs.imwrite("contours.png", result);


        // Test write image
        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);

            final double area = contourArea(contour);
            if (area > contourThresh && area < conturMax) {
                Rect rect = boundingRect(contour);
                if (DRAW_RECTS) {
                    // TODO: convert this code
//                    rectangle(screen,
//                            new Point(rect.x(), rect.y()),
//                            new Point(rect.x() + rect.width(), rect.y() + rect.height()),
//                            new Scalar(0, 0, 255));
                }
                sprites.add(new Point((int)(rect.x() + 0.5 * rect.width()), (int)(rect.y() + 0.5 * rect.height())));
            }
        }
        //Imgcodecs.imwrite("test.png", screenMat);

        return sprites;
    }
}
