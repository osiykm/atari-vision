package ale.agents;

import ale.burlap.AtariEnvironment;
import ale.burlap.SIDomainGenerator;
import ale.io.Actions;
import ale.io.ConsoleRAM;
import ale.io.RLData;
import ale.screen.ScreenMatrix;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * Created by MelRod on 3/13/16.
 */
public abstract class BURLAPAgent extends AbstractAgent {

    public AtariEnvironment environment;
    public LearningAgent learningAgent;
    public Domain domain;


    boolean closed = false;

    public BURLAPAgent(boolean useGUI) {
        super(useGUI);

        SIDomainGenerator domainGen = new SIDomainGenerator();
        this.domain = domainGen.generateDomain();

        this.environment = new AtariEnvironment();

        this.learningAgent = generateLearningAgent();
    }

    public abstract LearningAgent generateLearningAgent();

    /** The main program loop. In turn, we will obtain a new screen from ALE,
     *    pass it on to the agent and send back an action (which may be a reset
     *    request).
     */
    @Override
    public void run() {
        updateState();

        // Loop until we're done
        while (!closed) {

            // Run learning episode
            learningAgent.runLearningEpisode(environment);

            // auto-reset
            reset();
        }

        // Clean up the GUI
        ui.die();
    }

    public void updateState() {
        // Obtain relevant data from ALE
        closed = io.observe();
        // Obtain the screen matrix
        ScreenMatrix screen = io.getScreen();
        // Pass it on to UI
        updateImage(screen);
        // Get RLData
        RLData rlData = io.getRLData();

        // Update Environment State
        environment.setCurrentStateFromScreen(screen);
        environment.setLastReward(rlData.reward);
        environment.setTerminalState(rlData.isTerminal);
    }

    public void executeAction(GroundedAction a) {
        // perform action
        int action = Actions.map(a.actionName());
        io.act(action);

        // update state
        updateState();
    }

    public void reset() {
        io.act(Actions.map("system_reset"));
        updateState();
    }

    @Override
    public long getPauseLength() {
        return 0;
    }

    @Override
    public int selectAction() {
        return 0;
    }

    @Override
    public void observe(ScreenMatrix screen, ConsoleRAM ram, RLData rlData) {}

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    @Override
    public boolean wantsScreenData() {
        return true;
    }

    @Override
    public boolean wantsRamData() {
        return false;
    }
}
