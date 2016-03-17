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
package ale.agents;

import ale.cv.Sprite;
import ale.io.ConsoleRAM;
import ale.io.RLData;
import ale.movie.MovieGenerator;
import ale.screen.ScreenMatrix;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

import ale.cv.SpriteFinder;
import org.opencv.core.Point;

/** An 'agent' meant to be controlled by a human. Used to play the game and
 *   demonstrate the GUI.
 *
 * @author Marc G. Bellemare
 */
public class HumanAgent extends AbstractAgent {
    /** The object used to save frames to the disk */
    protected MovieGenerator movieGenerator;

    /** Variables to enforce 60 frames per second */
    protected long lastFrameTime;
    protected long lastWaitTime;
    protected final int framesPerSecond = 60;
    protected long millisFraction = 0;
    protected long timeError = 0;

    /** Variables to display relevant RL information */
    protected int rewardDisplayCounter = 0;
    protected int lastReward = 0;

    /** Keep track of whether we told the user that the game is over */
    protected boolean displayedGameOver = false;
    
    protected int numFramesToDisplayRewardFor = framesPerSecond * 1;

    public HumanAgent() {
        super();
    }

    public HumanAgent(boolean useGUI) {
        super(useGUI);
    }

    public boolean wantsScreenData() {
        return true;
    }

    public boolean wantsRamData() {
        return false;
    }

    public boolean shouldTerminate() {
        // Terminate if the 'q' key was pressed on the UI
        return ui.quitRequested();
    }

    @Override
    public long getPauseLength() {
        // The idea here is to try and compensate for I/O delays by adjusting
        //  the pause length from step to step
        long targetDelta = 1000 / framesPerSecond;
        long deltaRemainder = 1000 % framesPerSecond;
        millisFraction += deltaRemainder;

        // Correct for fractional deltas
        while (millisFraction > framesPerSecond) {
            targetDelta += 1;
            millisFraction -= framesPerSecond;
        }
        
        long time = System.currentTimeMillis();
        if (lastFrameTime == 0) {
            timeError += targetDelta;
        }
        else {
            long deltaTime = time - lastFrameTime;
            // Correct the timing by how much elapsed
            timeError += targetDelta - (deltaTime - lastWaitTime);
        }

        lastFrameTime = time;

        if (timeError > 0) {
            lastWaitTime = timeError;
            timeError = 0;
            return lastWaitTime;
        }
        else { // Don't wait if we're behind
            lastWaitTime = 0;
            return 0;
        }
    }

    @Override
    public int selectAction() {
        // Obtain the action from the UI
        int action = ui.getKeyboardAction();

        return action;
    }
    
    @Override
    public void observe(ScreenMatrix screen, ConsoleRAM ram, RLData rlData) {
        // Find sprites
        Map<Sprite, ArrayList<Point>> sprites = this.spriteFinder.findSprites(screen);

        // Display reward information via messages
        if (rlData.reward != 0)
            ui.addMessage("Reward: "+rlData.reward);
        // Also print out 'game over' when we received the terminal bit
        if (rlData.isTerminal) {
            if (!displayedGameOver) {
                ui.addMessage("GAME OVER");
                displayedGameOver = true;
            }
        }
        else
            displayedGameOver = false;
    }
}
