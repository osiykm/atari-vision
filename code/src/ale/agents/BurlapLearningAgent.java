package ale.agents;

import ale.burlap.ALEState;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.Domain;
import burlap.oomdp.visualizer.Visualizer;

/**
 * Created by MelRod on 4/21/16.
 */
public class BurlapLearningAgent extends BurlapAgent {

    LearningAgent learningAgent;

    public BurlapLearningAgent(LearningAgent learningAgent, Domain domain, ALEState initialState, Visualizer vis, boolean useGUI) {
        super(null, domain, initialState, vis, useGUI);

        this.learningAgent = learningAgent;
    }

    @Override
    public void runEpisode() {
        learningAgent.runLearningEpisode(this.env);
    }
}
