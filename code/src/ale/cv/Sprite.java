package ale.cv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that encapsulates a sprite.
 * Stores both of the sprites animation frames as well as
 * its point value.
 */
public class Sprite {
    private List<Mat> frames;
    private int value;

    /**
     * Loads a sprite from a path and a point value.
     * @param path The only frame for this sprite.
     * @param value The value of the sprite.
     */
    public Sprite(String path, int value) {
        this.frames = new ArrayList<>();
        Mat frame = Imgcodecs.imread(path);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
        frames.add(frame);
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
        Mat frame = Imgcodecs.imread(path2);
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
        this.frames.add(frame);
    }

    public Mat getFrame(int idx) {
        return this.frames.get(idx);
    }

    public int numFrames() {
        return this.frames.size();
    }

    public int getValue() {
        return value;
    }
}
