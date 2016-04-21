package ale.agents;

import ale.burlap.ALEState;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.visualizer.Visualizer;

/**
 * Created by MelRod on 4/21/16.
 */
public class BurlapLearningAgent extends BurlapAgent {

    LearningAgent learningAgent;

    public BurlapLearningAgent(Policy policy, LearningAgent learningAgent, Domain domain, ALEState initialState, Visualizer vis, boolean useGUI) {
        super(policy, domain, initialState, vis, useGUI);

        this.learningAgent = learningAgent;
    }

    @Override
    public void runEpisode() {
        learningAgent.runLearningEpisode(this.env);
    }
}
