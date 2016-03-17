package ale.cv;

import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import javafx.stage.Screen;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SpriteFinder {
    private List<Sprite> sprites;
    private ScreenConverter screenConverter;

    // Template detection thresholding
    private double threshold = 0.95;

    public SpriteFinder() {
        // Load sprites
        Sprite bottom = new Sprite("../sprites/1_left.png", "../sprites/1_right.png", 5);
        Sprite second = new Sprite("../sprites/2_left.png", "../sprites/2_right.png", 5);
        Sprite third = new Sprite("../sprites/3_left.png", "../sprites/3_right.png", 5);
        Sprite fourth = new Sprite("../sprites/4_left.png", "../sprites/4_right.png", 5);
        Sprite fifth = new Sprite("../sprites/5_left.png", "../sprites/5_right.png", 5);
        Sprite top = new Sprite("../sprites/6_left.png", "../sprites/6_right.png", 5);
        this.sprites = new ArrayList<>();
        sprites.add(bottom);
        sprites.add(second);
        sprites.add(third);
        sprites.add(fourth);
        sprites.add(fifth);
        sprites.add(top);
        screenConverter = new ScreenConverter(new NTSCPalette());
    }

    public Map<Sprite, ArrayList<Point>> findSprites(ScreenMatrix screen) {
        // Create empty hash map to store results
        HashMap<Sprite, ArrayList<Point>> spriteMap = new HashMap<>();

        // Convert screen to a OpenCV matrix
        Mat screenMat = screenConverter.convertMat(screen);


        for (Sprite sprite : this.sprites) {
            // Grab first frame
            Mat frame = sprite.getFrame1();

            // Create image with match
            int resultRows = screenMat.rows() - frame.rows() + 1;
            int resultCols = screenMat.cols() - frame.cols() + 1;
            Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

            // Run comparison on first frame
            Imgproc.matchTemplate(screenMat, frame, result, Imgproc.TM_CCOEFF_NORMED);

            // Check for matches
            ArrayList<Point> matches = checkMatches(result);

            // If no matches, possibly check second frame
            if (sprite.numFrames() == 2 && matches.isEmpty()) {
                frame = sprite.getFrame2();
                Imgproc.matchTemplate(screenMat, frame, result, Imgproc.TM_CCOEFF_NORMED);
                matches = checkMatches(result);
            }

            spriteMap.put(sprite, matches);
        }

        return spriteMap;
    }

    private ArrayList<Point> checkMatches(Mat result) {
        ArrayList<Point> matches = new ArrayList<>();

        for (int x = 0; x < result.rows(); x++) {
            for (int y = 0; y < result.cols(); y++) {
                if (result.get(x, y)[0] > this.threshold) {
                    matches.add(new Point(x, y));
                }
            }
        }

        return matches;
    }
}