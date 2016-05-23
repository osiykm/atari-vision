package ale.burlap;

import ale.gui.AgentGUI;
import ale.io.ALEDriver;
import ale.io.ALEPipes;
import ale.io.Actions;
import ale.io.RLData;
import ale.movie.MovieGenerator;
import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.io.IOException;

/**
 * Created by MelRod on 3/18/16.
 */
public class ALEEnvironment implements Environment {

    MovieGenerator movieGenerator;
    ScreenConverter screenConverter;
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
        initIO();

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
        ScreenMatrix screen = io.getScreen();
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
            movieGenerator.record(screenConverter.convert(screen));
        }
    }

    @Override
    public State getCurrentObservation() {
        return currentState;
    }

    @Override
    public EnvironmentOutcome executeAction(GroundedAction ga) {
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
    public double getLastReward() {
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
    protected void initIO() {
        io = null;

        try {
            // Initialize the pipes; use named pipes if requested
            io = new ALEDriver(rom);

            // Determine which information to request from ALE
            io.setUpdateScreen(true);
            io.setUpdateRL(true);
            io.setUpdateRam(false);
            io.initPipes();
        }
        catch (IOException e) {
            System.err.println ("Could not initialize pipes: "+e.getMessage());
            System.exit(-1);
        }

        // if we are saving the screen buffers, init relevant objects
        if (movieOutputFile != null) {
            movieGenerator = new MovieGenerator(movieOutputFile);
            screenConverter = new ScreenConverter(new NTSCPalette());
        }
    }
}
