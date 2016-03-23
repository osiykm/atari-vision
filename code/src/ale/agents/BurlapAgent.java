package ale.agents;

import ale.burlap.ALEEnvironment;
import ale.burlap.ALEState;
import burlap.behavior.policy.Policy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

/**
 * Created by MelRod on 3/22/16.
 */
public class BurlapAgent {

    Policy policy;
    Domain domain;
    ALEEnvironment env;
    Visualizer vis;

    public BurlapAgent(Policy policy, Domain domain, ALEState initialState, Visualizer vis, boolean useGUI) {

        this.domain = domain;
        this.policy = policy;
        this.vis = vis;

        env = new ALEEnvironment(domain, initialState, useGUI);
    }

    public void run(int episodes) {
        if (vis != null) {
            VisualExplorer exp = new VisualExplorer(domain, env, vis);
            exp.initGUI();
            exp.startLiveStatePolling(10);
        }

        for (int e = 1 ; e <= episodes ; e++) {
            runEpisode();
        }

        // Clean up the environment
        env.die();

    }

    public void runEpisode() {
        while(!env.isInTerminalState()){
            State curState = env.getCurrentObservation();
            GroundedAction action = (GroundedAction)policy.getAction(curState);

            action.executeIn(env);
        }
    }
}
