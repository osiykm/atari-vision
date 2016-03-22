package ale.cv;

import ale.burlap.ALEDomainConstants;
import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SpriteFinder {
    private List<Sprite> sprites;
    private ScreenConverter screenConverter;

    // Template detection thresholding
    private double threshold = 0.85;

    // shared SpriteFinder
    private static SpriteFinder spriteFinder;
    public static SpriteFinder getSpriteFinder() {
        if (spriteFinder == null) {
            spriteFinder = new SpriteFinder();
        }
        return spriteFinder;
    }

    public SpriteFinder() {
        // Load sprites
        this.sprites = new ArrayList<>();

        // player
        Sprite player = new Sprite(ALEDomainConstants.CLASSAGENT, "../sprites/player.jpg");
        sprites.add(player);

        // aliens
        Sprite bottom = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/1_left.jpg", "../sprites/1_right.jpg");
        Sprite second = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/2_left.jpg", "../sprites/2_right.jpg");
        Sprite third = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/3_left.jpg", "../sprites/3_right.jpg");
        Sprite fourth = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/4_left.jpg", "../sprites/4_right.jpg");
        Sprite fifth = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/5_left.jpg", "../sprites/5_right.jpg");
        Sprite top = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/6_left.jpg", "../sprites/6_right.jpg");
        sprites.add(bottom);
        sprites.add(second);
        sprites.add(third);
        sprites.add(fourth);
        sprites.add(fifth);
        sprites.add(top);

        // bomb
//        Sprite bomb = new Sprite(ALEDomainConstants.CLASSBOMB, "../sprites/bomb.jpg");
//        sprites.add(bomb);


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