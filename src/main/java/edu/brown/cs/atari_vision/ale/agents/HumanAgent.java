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
package edu.brown.cs.atari_vision.ale.agents;

import edu.brown.cs.atari_vision.ale.io.ConsoleRAM;
import edu.brown.cs.atari_vision.ale.io.RLData;
import edu.brown.cs.atari_vision.ale.movie.MovieGenerator;
import edu.brown.cs.atari_vision.ale.screen.NTSCPalette;
import edu.brown.cs.atari_vision.ale.screen.ScreenConverter;
import edu.brown.cs.atari_vision.ale.screen.ScreenMatrix;
import org.bytedeco.javacpp.opencv_core.*;

/** An 'agent' meant to be controlled by a human. Used to play the game and
 *   demonstrate the GUI.
 *
 * @author Marc G. Bellemare
 */
public class HumanAgent extends AbstractAgent {
    /** The object used to save frames to the disk */
    protected MovieGenerator movieGenerator;
    String movieOutputFile = null;// "./movies/human/atari_";

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

    public HumanAgent(String rom) {
        super(rom);

        if (movieOutputFile != null) {
            movieGenerator = new MovieGenerator(movieOutputFile);
        }
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
    public void observe(Mat screen, ConsoleRAM ram, RLData rlData) {
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

        // Save screen capture
        if (movieGenerator != null) {
            movieGenerator.record(ScreenConverter.convert(screen));
        }
    }

    public static void main(String[] args) {
        String rom = "pong.bin";

        HumanAgent agent = new HumanAgent(rom);
        agent.run();
    }
}
