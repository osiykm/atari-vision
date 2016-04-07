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

import ale.gui.AgentGUI;
import ale.io.ALEPipes;
import ale.io.Actions;
import ale.io.ConsoleRAM;
import ale.io.RLData;
import ale.screen.ScreenMatrix;

import java.io.IOException;

/** An abstract agent class. New agents can be created by extending this class
 *   and implementing its abstract methods.
 * 
 * @author Marc G. Bellemare
 */
public abstract class AbstractAgent {

    /** The UI used for displaying images and receiving actions */
    protected AgentGUI ui;
    /** The I/O object used to communicate with ALE */
    protected ALEPipes io;

    /** Parameters */
    /** Whether to use a GUI */
    protected boolean useGUI;

    /** Create a new agent that communicates with ALE via stdin/out and
     *    uses the graphical user interface.
     */
    public AbstractAgent() {
        this(true);
    }

    /** Create a new agent with the specified parameters. The user can specify
     *   the base name for two FIFO pipes used to communicate with ALE. If
     *   namedPipesBasename is not null, then the files namedPipesBasename+"_in"
     *   and namedPipesBasename+"_out" are read and written to by the agent.
     *   See ALE documentation for more details on running with named pipes.
     *
     * @param useGUI If true, a GUI is used to display received screen data.
     */
    public AbstractAgent(boolean useGUI) {
        this.useGUI = useGUI;
        
        init();
    }

    /** Initialize relevant bits of the agent
     * 
     */
    public final void init() {
        if (useGUI) {
            // Create the GUI
            ui = new AgentGUI();
        }

        // Create the relevant I/O objects
        initIO();
    }

    /** Initialize the I/O object for this agent.
     * 
     */
    protected void initIO() {
        io = null;

        try {
            // Initialize the pipes; use named pipes if requested
            io = new ALEPipes();

            // Determine which information to request from ALE
            io.setUpdateScreen(useGUI || wantsScreenData());
            io.setUpdateRam(wantsRamData());
            io.setUpdateRL(wantsRLData());
            io.initPipes();
        }
        catch (IOException e) {
            System.err.println ("Could not initialize pipes: "+e.getMessage());
            System.exit(-1);
        }
    }

    /** The main program loop. In turn, we will obtain a new screen from ALE,
     *    pass it on to the agent and send back an action (which may be a reset
     *    request).
     */
    public void run() {
        boolean done = false;

        // Loop until we're done
        while (!done) {
            // Obtain relevant data from ALE
            done = io.observe();
            // The I/O channel will return true once EOF is received
            if (done) break;
            
            // Obtain the screen matrix
            ScreenMatrix screen = io.getScreen();
            // Pass it on to UI
            ui.updateImage(screen);

            // Pass screen matrix to agent
            RLData rlData = io.getRLData();
            observe(screen, io.getRAM(), rlData);

            // auto-reset if terminal
            if (rlData.isTerminal) {
                io.act(Actions.map("system_reset"));
                continue;
            }

            // Request an action from the agent
            int action = selectAction();
            // Send it back to ALE
            done = io.act(action);

            // Ask the agent whether it wants us to pause
            long pauseLength = getPauseLength();
            // If so, pause!
            if (pauseLength > 0) {
                pause(pauseLength);
            }

            // The agent also tells us when to terminate
            done |= shouldTerminate();
        }

        // Clean up the GUI
        ui.die();
    }

    protected void pause(long waitTime) {
        try {
            Thread.sleep(waitTime);
        }
        catch (Exception e) {
        }
    }

    /** Returns how long to pause for, in milliseconds, before the next time step.
     *
     * @return
     */
    public abstract long getPauseLength();
    /** Returns the agent's next action.
     *
     * @return
     */
    public abstract int selectAction();
    /** Provides the agent with the latest screen, RAM and RL data.
     * 
     * @param screen
     * @param ram
     * @param rlData
     */
    public abstract void observe(ScreenMatrix screen, ConsoleRAM ram, RLData rlData);
    /** Returns true to indicate that we should exit the program.
     * 
     * @return
     */
    public abstract boolean shouldTerminate();
    /** Returns true if we want to receive the screen matrix from ALE.
     *
     * @return
     */
    public abstract boolean wantsScreenData();
    /** Returns true if we want to receive the RAM from ALE.
     *
     * @return
     */
    public abstract boolean wantsRamData();
    /** Returns true if we want to receive RL data from ALE.
     * 
     * @return
     */
    public boolean wantsRLData() {
        return true;
    }
}
