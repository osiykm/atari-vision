package ale.cv;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * A class that encapsulates a sprite.
 * Stores both of the sprites animation frames as well as
 * its point value.
 */
public class Sprite {
    private Mat frame1;
    private Mat frame2;
    private int value;

    /**
     * Loads a sprite from a path and a point value.
     * @param path The only frame for this sprite.
     * @param value The value of the sprite.
     */
    public Sprite(String path, int value) {
        this.frame1 = Imgcodecs.imread(path);
        this.value = value;
    }

    /**
     * Loads a sprite from two paths and a point value
     * @param path1 The first frame of the sprite's animation.
     * @param path2 The second frame of the sprite's animation.
     * @param value The value of the sprite
     */
    public Sprite(String path1, String path2, int value) {
        this(path1, value);
        this.frame2 = Imgcodecs.imread(path2);
    }

    public int numFrames() {
        return this.frame2 == null ? 1 : 2;
    }

    public Mat getFrame1() {
        return frame1;
    }

    public Mat getFrame2() {
        return frame2;
    }

    public int getValue() {
        return value;
    }
}
