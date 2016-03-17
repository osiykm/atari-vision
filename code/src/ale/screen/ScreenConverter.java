/*
 * Java Arcade Learning Environment (A.L.E) Agent
 *  Copyright (C) 2011-2012 Marc G. Bellemare <mgbellemare@ualberta.ca>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ale.screen;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Converts a ScreenMatrix to a BufferedImage, using a ColorMap.
 *
 * @author Marc G. Bellemare <mgbellemare@ualberta.ca>
 */
public class ScreenConverter {
    /** The map from screen indices to RGB colors */
    protected ColorPalette colorMap;

    /** Create a new ScreenConverter with the desired color palette
     * 
     * @param cMap
     */
    public ScreenConverter(ColorPalette cMap) {
        colorMap = cMap;
    }

    /** Transforms a ScreenMatrix into a BufferedImage.
     * 
     * @param m
     * @return
     */
    public BufferedImage convert(ScreenMatrix m) {
        // Create a new image, of the same width and height as the screen matrix
        BufferedImage img = new BufferedImage(m.width, m.height, BufferedImage.TYPE_INT_RGB);

        // Map each pixel
        for (int x = 0; x < m.width; x++)
            for (int y = 0; y < m.height; y++) {
                int index = m.matrix[x][y];
                Color c = colorMap.get(index);
                img.setRGB(x, y, c.getRGB());
            }

        return img;
    }

    public Mat convertMat(ScreenMatrix m) {
        // Create a new Mat of the same width and height as the screen matrix
        // Use the 8-bit (one byte) unsigned, 3 channel format
        Mat img = new Mat(m.height, m.width, CvType.CV_8UC3);
        int channels = img.channels();
        byte[] data = new byte[m.width * m.height * channels];

        // Map each pixel
        for (int x = 0; x < m.width; x++) {
            for (int y = 0; y < m.height; y++) {
                int index = m.matrix[x][y];
                Color c = colorMap.get(index);
                // OpenCV matrices are stored column major
                // So, not great cache locality currently...
                int offset = ((y * m.width) + x) * channels;
                data[offset] = (byte) c.getBlue();
                data[offset + 1] = (byte) c.getGreen();
                data[offset + 2] = (byte) c.getRed();
            }
        }

        // Place bytes into the Mat
        img.put(0, 0, data);

        return img;
    }
}
