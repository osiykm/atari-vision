package ale.cv;

import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class SpriteFinder {
    // List of active sprites to search for
    private List<Sprite> sprites;

    // Number of missing frames per sprite
    private Map<Sprite, Integer> missingFrames;
    private int maxFrames = 3;

    // Converter for ScreenMatrix to OpenCV Mat
    private ScreenConverter screenConverter;

    // Template detection thresholding
    private double threshold = 0.05;

    // Last frame with sucessful matches
    private int lastFrame = 0;

    public SpriteFinder() {
        // Load sprites
        Sprite bottom = new Sprite("../sprites/1_left.png", "../sprites/1_right.png", 5);
        Sprite second = new Sprite("../sprites/2_left.png", "../sprites/2_right.png", 5);
        Sprite third = new Sprite("../sprites/3_left.png", "../sprites/3_right.png", 5);
        Sprite fourth = new Sprite("../sprites/4_left.png", "../sprites/4_right.png", 5);
        Sprite fifth = new Sprite("../sprites/5_left.png", "../sprites/5_right.png", 5);
        Sprite top = new Sprite("../sprites/6_left.png", "../sprites/6_right.png", 5);
        this.sprites = new ArrayList<>();
        this.missingFrames = new HashMap<>();
        sprites.add(bottom);
        missingFrames.put(bottom, 0);
        sprites.add(second);
        missingFrames.put(second, 0);
        sprites.add(third);
        missingFrames.put(third, 0);
        sprites.add(fourth);
        missingFrames.put(fourth, 0);
        sprites.add(fifth);
        missingFrames.put(fifth, 0);
        sprites.add(top);
        missingFrames.put(top, 0);
        screenConverter = new ScreenConverter(new NTSCPalette());
    }

    public Map<Sprite, ArrayList<Point>> findSprites(ScreenMatrix screen) {
        // Create empty hash map to store results
        HashMap<Sprite, ArrayList<Point>> spriteMap = new HashMap<>();

        // Convert screen to a OpenCV matrix
        Mat screenMat = screenConverter.convertMat(screen);
        Imgproc.cvtColor(screenMat, screenMat, Imgproc.COLOR_BGR2GRAY);

        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()) {
            Sprite sprite = iterator.next();
            // Grab first frame
            Mat frame = sprite.getFrame(lastFrame);

            // Create image with match
            int resultRows = screenMat.rows() - frame.rows() + 1;
            int resultCols = screenMat.cols() - frame.cols() + 1;
            Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

            // Run comparison on first frame
            Imgproc.matchTemplate(screenMat, frame, result, Imgproc.TM_SQDIFF_NORMED);

            // Check for matches
            ArrayList<Point> matches = checkMatches(result);

            // If no matches, possibly check next frame
            if (sprite.numFrames() >= lastFrame && matches.isEmpty()) {
                this.lastFrame = 1 - this.lastFrame;
                frame = sprite.getFrame(lastFrame);
                Imgproc.matchTemplate(screenMat, frame, result, Imgproc.TM_SQDIFF_NORMED);
                matches = checkMatches(result);
            }

            // Check for any matches
            if (!matches.isEmpty()) {
                spriteMap.put(sprite, matches);
                missingFrames.put(sprite, 0);
            } else {
                if (missingFrames.get(sprite) == this.maxFrames) {
                    iterator.remove();
                } else {
                    missingFrames.put(sprite, missingFrames.get(sprite) + 1);
                }
            }
        }

        return spriteMap;
    }

    private ArrayList<Point> checkMatches(Mat result) {
        ArrayList<Point> matches = new ArrayList<>();

        for (int x = 0; x < result.rows(); x++) {
            for (int y = 0; y < result.cols(); y++) {
                if (result.get(x, y)[0] < this.threshold) {
                    /*
                     * NOTE:
                     * These coordinates may be dumb, i.e. not correspond to actual screen coordinates
                     * because of how the result image looks.
                     *
                     * That said, they *should* work for basic learning.
                     */
                    matches.add(new Point(x, y));
                }
            }
        }
        return matches;
    }
}