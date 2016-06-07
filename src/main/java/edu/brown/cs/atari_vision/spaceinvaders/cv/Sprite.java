package edu.brown.cs.atari_vision.spaceinvaders.cv;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

/**
 * A class that encapsulates a sprite.
 * Stores both of the sprites animation frames as well as
 * its point value.
 */
public class Sprite {
    private String id;
    private Mat frame1;
    private Mat frame2;
    public int width;
    public int height;

    /**
     * Loads a sprite from a path and a point value.
     * @param id The object id of the sprite
     * @param path The only frame for this sprite.
     */
    public Sprite(String id, String path) {
        this.id = id;
        this.frame1 = imread(path);
        this.width = this.frame1.size().width();
        this.height = this.frame1.size().height();
    }

    /**
     * Loads a sprite from two paths and a point value
     * @param id The object id of the sprite
     * @param path1 The first frame of the sprite's animation.
     * @param path2 The second frame of the sprite's animation.
     */
    public Sprite(String id, String path1, String path2) {
        this(id, path1);
        this.frame2 = imread(path2);
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

    public String getId() {
        return id;
    }
}