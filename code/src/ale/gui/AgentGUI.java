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
package ale.gui;

import ale.screen.ColorPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;

import javax.swing.*;
import java.awt.image.BufferedImage;

/** GUI for the Java ALE agent.
 * 
 * @author Marc G. Bellemare
 */
public final class AgentGUI extends JFrame implements AbstractUI {
    /** An object in which we display the screen image */
    protected final ScreenDisplay panel;
    /** An object that listens for key presses */
    protected final KeyboardControl keyboard;
    /** Used to convert ALE screen data to GUI images */
    protected final ScreenConverter converter;

    /** Create a new GUI
     * 
     */
    public AgentGUI(){
        // Create the color palette we will use to interpret ALE data
        ColorPalette palette = ColorPalette.makePalette("NTSC");

        // Create an object to convert indexed images to Java images
        converter = new ScreenConverter(palette);

        // Create the keyboard and image panel
        keyboard = new KeyboardControl();
        panel = new ScreenDisplay();
        add(panel);

        this.addKeyListener(keyboard);
        this.setSize(panel.getPreferredSize());

        pack();
        setLocationRelativeTo(null);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /** Internal method to update the image displayed in the GUI.
     *
     * @param currentScreen
     */
    public void updateImage(ScreenMatrix currentScreen) {
        // Convert the screen matrix to an image
        BufferedImage img = converter.convert(currentScreen);

        // Provide the new image to the UI
        updateFrameCount();
        setImage(img);
        refresh();
    }

    /** When die() is called, we want to safely close the GUI */
    public void die() {
        this.dispose();
    }

    public void setImage(BufferedImage img) {
        panel.setImage(img);
    }

    public void setCenterString(String s) {
        panel.setCenterString(s);
    }

    public void addMessage(String s) {
        panel.addMessage(s);
    }

    public int getKeyboardAction() {
        return keyboard.toALEAction();
    }

    public void updateFrameCount() {
        panel.updateFrameCount();
    }

    public boolean quitRequested() {
        return (keyboard.quit == true);
    }

    public void refresh() {
        this.repaint();
    }
}
