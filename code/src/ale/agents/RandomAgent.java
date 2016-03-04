package ale.agents;

import ale.io.Actions;
import ale.io.ConsoleRAM;
import ale.io.RLData;
import ale.screen.ScreenMatrix;

/**
 * Created by MelRod on 3/4/16.
 */
public class RandomAgent extends AbstractAgent {

    protected final int numActions = Actions.numPlayerActions;

    public RandomAgent(boolean useGUI) {
        super(useGUI);
    }

    @Override
    public long getPauseLength() {
        return 0;
    }

    @Override
    public int selectAction() {
        int a = (int)(Math.random() * numActions);

        return a;
    }

    @Override
    public void observe(ScreenMatrix screen, ConsoleRAM ram, RLData rlData) {

    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    @Override
    public boolean wantsScreenData() {
        return false;
    }

    @Override
    public boolean wantsRamData() {
        return false;
    }
}
