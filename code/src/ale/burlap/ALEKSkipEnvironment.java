package ale.burlap;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

/**
 * Created by MelRod on 5/27/16.
 */
public class ALEKSkipEnvironment extends ALEEnvironment {

    int k; // every action is repeated 

    public ALEKSkipEnvironment(Domain domain, ALEState initialState, int k, String rom, boolean useGUI) {
        super(domain, initialState, rom, useGUI);
        
        this.k = k;
        if (k < 1) {
            throw new IllegalArgumentException("k must be greater than or equal to 1");
        }
    }

    @Override
    public EnvironmentOutcome executeAction(GroundedAction ga) {
        double totalReward = 0;
        State startState = getCurrentObservation();

        for(int i = 0; i < k; i++) {
            EnvironmentOutcome eo = super.executeAction(ga);
            totalReward += eo.r;
        }

        return new EnvironmentOutcome(startState, ga, getCurrentObservation(), totalReward, isInTerminalState());
    }
}
