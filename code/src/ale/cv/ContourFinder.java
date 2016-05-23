package ale.cv;

import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

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
        screenConverter = new ScreenConverter(new NTSCPalette());
    }

    public List<Point> findSprites(ScreenMatrix screen) {
        ArrayList<Point> sprites = new ArrayList<>();

        // Convert screen to Mat
        Mat screenMat = screenConverter.convertMat(screen);
        Mat grey = new Mat();
        Mat filtered = new Mat();
        Mat mask = new Mat();
        Mat result = new Mat();
        Mat gradX = new Mat();
        Mat gradY = new Mat();

        // Filter out ground
        Core.inRange(screenMat, new Scalar(18, 80, 75), new Scalar(25, 90, 85), mask);
        //Imgcodecs.imwrite("mask.png", mask);
        Core.bitwise_not(mask, mask);
        screenMat.copyTo(filtered, mask);

        // Convert to grey
        Imgproc.cvtColor(filtered, grey, Imgproc.COLOR_BGR2GRAY);
        //Imgcodecs.imwrite("bw.png", grey);

        // Perform thresholding
        Imgproc.adaptiveThreshold(grey, result, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                threshSize, threshBias);
        //Imgcodecs.imwrite("thresh.png", result);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(result, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
        //Imgcodecs.imwrite("contours.png", result);


        // Test write image
        for (MatOfPoint contour : contours) {
            final double area = Imgproc.contourArea(contour);
            if (area > contourThresh && area < conturMax) {
                Rect rect = Imgproc.boundingRect(contour);
                if (DRAW_RECTS) {
                    Imgproc.rectangle(screenMat,
                            new Point(rect.x, rect.y),
                            new Point(rect.x + rect.width, rect.y + rect.height),
                            new Scalar(0, 0, 255));
                }
                sprites.add(new Point(rect.x + 0.5 * (float) rect.width, rect.y + 0.5 * (float) rect.height));
            }
        }
        //Imgcodecs.imwrite("test.png", screenMat);

        return sprites;
    }
}
