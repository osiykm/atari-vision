package ale.cv;

import ale.burlap.ALEDomainConstants;
import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alex on 14/4/16.
 */
public class TargetedContourFinder {
    /** Object for converting ScreenMatrix to OpenCV Mat. */
    private ScreenConverter screenConverter;

    // Scalars
    private final Scalar ALIEN_COLOR = new Scalar(29, 134, 134);
    private final Scalar PLAYER_COLOR = new Scalar(50, 132, 50);
    private final Scalar BOMB_COLOR = new Scalar(40, 83, 181);

    // Shared targeted sprite finder
    private static TargetedContourFinder tcf = new TargetedContourFinder();
    public static TargetedContourFinder getTCF() {
        return tcf;
    }

    public TargetedContourFinder() {
        screenConverter = new ScreenConverter(new NTSCPalette());
    }

    /**
     * Finds center points for sprites within an image, searching for only a given color.
     * @param mat a binary image to find contours in.
     * @param color The color to search for in the image.
     * @param min The maximum size of a contour in the image.
     * @param max The maximimum size of a contour in the image.
     * @return A list of points, each centered on one fo the bounding boxes found in the image.
     */
    private ArrayList<Point> findSpritePoints(Mat mat, Scalar color, double min, double max) {
        // Convert image to binary
        Mat binary = new Mat();
        Core.inRange(mat, color, color, binary);

        // Find contours
        ArrayList<Point> points = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binary, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
        for (MatOfPoint contour : contours) {
            final double area = Imgproc.contourArea(contour);
            if (area >= min && area <= max) {
                Rect rect = Imgproc.boundingRect(contour);
                points.add(new Point(rect.x + 0.5 * (float) rect.width,
                                     rect.y + 0.5 * (float) rect.height));
            }
        }
        return points;
    }

    public HashMap<String, List<Point>> findSprites(ScreenMatrix screen) {
        // List to contain all lists of sprite locations
        HashMap<String, List<Point>> locations = new HashMap<>();

        // Convert screen to Mat
        Mat screenMat = screenConverter.convertMat(screen);

        // Filter to select only aliens
        ArrayList<Point> aliens = findSpritePoints(screenMat, ALIEN_COLOR, 8, 100);
        locations.put(ALEDomainConstants.CLASSALIEN, aliens);

        // Filter to select only player
        ArrayList<Point> player = findSpritePoints(screenMat, PLAYER_COLOR, 5, 50);
        locations.put(ALEDomainConstants.CLASSAGENT, player);

        // Filter to select only bombs
        ArrayList<Point> bombs = findSpritePoints(screenMat, BOMB_COLOR, 0, 5);
        locations.put(ALEDomainConstants.CLASS_BOMB_UNKNOWN, bombs);

        return locations;
    }
}
