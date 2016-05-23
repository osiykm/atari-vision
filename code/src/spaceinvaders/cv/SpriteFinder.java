package spaceinvaders.cv;

import ale.burlap.ALEDomainConstants;
import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Sprite player = new Sprite(ALEDomainConstants.CLASSAGENT, "../sprites/player.png");
        sprites.add(player);

        // aliens
        Sprite bottom = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/1_left.png", "../sprites/1_right.png");
        Sprite second = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/2_left.png", "../sprites/2_right.png");
        Sprite third = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/3_left.png", "../sprites/3_right.png");
        Sprite fourth = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/4_left.png", "../sprites/4_right.png");
        Sprite fifth = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/5_left.png", "../sprites/5_right.png");
        Sprite top = new Sprite(ALEDomainConstants.CLASSALIEN, "../sprites/6_left.png", "../sprites/6_right.png");
        sprites.add(bottom);
        sprites.add(second);
        sprites.add(third);
        sprites.add(fourth);
        sprites.add(fifth);
        sprites.add(top);

        // bomb
//        Sprite bomb = new Sprite(ALEDomainConstants.CLASSBOMB, "../sprites/bomb.png");
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
            ArrayList<Point> matches = checkMatches(result, sprite.width, sprite.height);

            // If no matches, possibly check second frame
            if (sprite.numFrames() == 2 && matches.isEmpty()) {
                frame = sprite.getFrame2();
                Imgproc.matchTemplate(screenMat, frame, result, Imgproc.TM_CCOEFF_NORMED);
                matches = checkMatches(result, sprite.width, sprite.height);
            }

            spriteMap.put(sprite, matches);
        }

        return spriteMap;
    }

    private ArrayList<Point> checkMatches(Mat result, int width, int height) {
        int halfWidth = width/2;
        int halfHeight = height/2;
        ArrayList<Point> matches = new ArrayList<>();

        for (int r = 0; r < result.rows(); r++) {
            for (int c = 0; c < result.cols(); c++) {
                if (result.get(r, c)[0] > this.threshold) {
                    // set point as center of sprite
                    matches.add(new Point(c + halfWidth, r + halfHeight));
                }
            }
        }

        return matches;
    }
}