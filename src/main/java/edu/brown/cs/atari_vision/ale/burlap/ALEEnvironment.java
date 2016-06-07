package edu.brown.cs.atari_vision.ale.burlap;

import burlap.mdp.core.Action;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import edu.brown.cs.atari_vision.ale.gui.AgentGUI;
import edu.brown.cs.atari_vision.ale.io.ALEDriver;
import edu.brown.cs.atari_vision.ale.io.Actions;
import edu.brown.cs.atari_vision.ale.io.RLData;
import edu.brown.cs.atari_vision.ale.movie.MovieGenerator;
import edu.brown.cs.atari_vision.ale.screen.NTSCPalette;
import edu.brown.cs.atari_vision.ale.screen.ScreenConverter;
import edu.brown.cs.atari_vision.ale.screen.ScreenMatrix;
import org.bytedeco.javacpp.opencv_core.*;

import java.io.IOException;

/**
 * @author Melrose Roderick
 */
public class ALEEnvironment implements Environment {

    MovieGenerator movieGenerator;
    String movieOutputFile = null;//"./movies/naive/atari_";

    /** The UI used for displaying images and receiving actions */
    private AgentGUI ui;
    /** The I/O object used to communicate with ALE */
    private ALEDriver io;

    /** State data **/
    Domain domain;
    private ALEState currentState;
    private int lastReward;
    private boolean isTerminal;

    /** Parameters */
    /** Whether to use a GUI */
    private boolean useGUI;
    private String rom;

    public ALEEnvironment(Domain domain, ALEState initialState, String rom, boolean useGUI) {
        this.rom = rom;
        this.useGUI = useGUI;
        if (this.useGUI) {
            // Create the GUI
            ui = new AgentGUI();
        }

        // Create the relevant I/O objects
        initIO(1);

        // Set initial state
        currentState = initialState;
        this.domain = domain;
        updateState();
    }

    public ALEEnvironment(Domain domain, ALEState initialState, String rom, int frameSkip, boolean useGUI) {
        this.rom = rom;
        this.useGUI = useGUI;
        if (this.useGUI) {
            // Create the GUI
            ui = new AgentGUI();
        }

        // Create the relevant I/O objects
        initIO(frameSkip);

        // Set initial state
        currentState = initialState;
        this.domain = domain;
        updateState();
    }

    private void updateState() {
        // Obtain relevant data from ALE
        boolean closed = io.observe();
        if (closed) {
            // the FIFO stream was closed
            // exit cleanly
            if (useGUI) {
                ui.die();
            }
            System.exit(0);
        }

        // Obtain the screen matrix
        Mat screen = io.getScreen();
        // Pass it on to UI
        if (useGUI) {
            ui.updateImage(screen);
        }

        // Get RLData
        RLData rlData = io.getRLData();

        // Update Environment State
        currentState = currentState.updateStateWithScreen(domain, screen);
        lastReward = rlData.reward;
        isTerminal = rlData.isTerminal;

        // Save screen capture
        if (movieGenerator != null) {
            movieGenerator.record(ScreenConverter.convert(screen));
        }
    }

    @Override
    public State currentObservation() {
        return currentState;
    }

    @Override
    public EnvironmentOutcome executeAction(Action ga) {
        // save start state
        ALEState startState = currentState;

        // perform action
        int action = Actions.map(ga.actionName());
        io.act(action);

        // update state
        updateState();

        return new EnvironmentOutcome(startState, ga, currentState, lastReward, isInTerminalState());
    }

    @Override
    public double lastReward() {
        return lastReward;
    }

    @Override
    public boolean isInTerminalState() {
        return isTerminal;
    }

    @Override
    public void resetEnvironment() {
        // perform reset action
        io.act(Actions.map("system_reset"));

        // update state
        updateState();
    }

    public void die() {
        if (useGUI) {
            ui.die();
        }
    }

    /** Initialize the I/O object.
     *
     */
    protected void initIO(int frameSkip) {
        io = null;

        try {
            // Initialize the pipes; use named pipes if requested
            io = new ALEDriver(rom);

            // Determine which information to request from ALE
            io.setUpdateScreen(true);
            io.setUpdateRL(true);
            io.setUpdateRam(false);
            io.setFrameSkip(frameSkip);
            io.initPipes();
        }
        catch (IOException e) {
            System.err.println ("Could not initialize pipes: "+e.getMessage());
            System.exit(-1);
        }

        // if we are saving the screen buffers, init relevant objects
        if (movieOutputFile != null) {
            movieGenerator = new MovieGenerator(movieOutputFile);
        }
    }
}
