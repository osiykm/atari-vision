package ale.burlap;

import ale.agents.BURLAPAgent;
import ale.screen.ScreenMatrix;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

/**
 * Created by MelRod on 3/13/16.
 */
public class AtariEnvironment implements Environment {

    public BURLAPAgent agent;

    public State currentState;
    public double lastReward;
    public boolean isTerminalState;

    public void setCurrentStateFromScreen(ScreenMatrix screen) {
        //TODO: implement
    }

    public void setLastReward(double reward) {
        this.lastReward = reward;
    }
    public void setTerminalState(boolean isTerminalState) {
        this.isTerminalState = isTerminalState;
    }

    @Override
    public State getCurrentObservation() {
        return currentState;
    }

    @Override
    public EnvironmentOutcome executeAction(GroundedAction ga) {
        State startState = currentState;

        agent.executeAction(ga);

        return new EnvironmentOutcome(startState, ga, currentState, this.lastReward, this.isInTerminalState());
    }

    @Override
    public double getLastReward() {
        return lastReward;
    }

    @Override
    public boolean isInTerminalState() {
        return isTerminalState;
    }

    @Override
    public void resetEnvironment() {
        agent.reset();
    }
}
