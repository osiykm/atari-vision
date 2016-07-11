package edu.brown.cs.atari_vision.caffe.visualizers;

import burlap.behavior.valuefunction.QValue;
import edu.brown.cs.atari_vision.ale.gui.ScreenDisplay;
import org.bytedeco.javacpp.caffe;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by maroderi on 7/8/16.
 */
public class DetectorVisualizer extends ScreenDisplay {

    static BufferedImage image;

    static int numTilesX = 10;
    static int numTilesY = 10;
    static int numDetectors = 3;
    static float[] detectorData = new float[numTilesX * numTilesY * numDetectors];

    public DetectorVisualizer() {
        super();

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                //refresh screen
                repaint();
            }
        };

        Timer t = new Timer(33, al);
        t.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.setImage(image);
        super.paintComponent(g);
        drawDetectors(g);
    }

    public void drawDetectors(Graphics g) {
        Graphics2D g2 = ( Graphics2D ) g;

        int height = this.getHeight();
        int width = this.getWidth();
        float tileWidth = (float)width/(float)numTilesX;
        float tileHeight = (float)height/(float)numTilesY;

        // make grid
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(2));
        for (int x = 1; x < numTilesX; x++) {
            g2.drawLine((int)(x*tileWidth), 0, (int)(x*tileWidth), height);
        }
        for (int y = 1; y < numTilesY; y++) {
            g2.drawLine(0, (int)(y*tileHeight), width, (int)(y*tileHeight));
        }

        float threshold = 0.5f;
        Color[] colors = {new Color(255,0,0,150), new Color(0,255,0,150), new Color(0,0,255,150)};
        int index = 0;
        for (int i = 0; i < numDetectors; i++) {
            g2.setColor(colors[i]);
            for (int r = 0; r < numTilesY; r++) {
                for (int c = 0; c < numTilesX; c++) {
                    float detect = detectorData[index++];
                    if (detect > 0) {
                        System.out.println("HERE");
                    }
                    if (detect > threshold) {
                        g2.fill(new Rectangle2D.Float(c * tileWidth, r * tileHeight, tileWidth, tileHeight));
                    }
                }
            }
        }
    }

    public static void updateImage(BufferedImage image) {
        DetectorVisualizer.image = image;
    }

    public static void updateDetectorData(caffe.FloatBlob blob) {
        int index = 0;
        for (int i = 0; i < numDetectors; i++) {
            for (int r = 0; r < numTilesY; r++) {
                for (int c = 0; c < numTilesX; c++) {
                    detectorData[index++] = blob.data_at(0, i, r, c);
                }
            }
        }
    }

    public static JFrame createDetectorVisualizer() {

        JFrame f = new JFrame("Detector Visualizer");
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.add(new DetectorVisualizer());
        f.pack();
        f.setLocationByPlatform(true);
        f.setResizable( true );
        f.setVisible( true );

        return f;
    }
}
