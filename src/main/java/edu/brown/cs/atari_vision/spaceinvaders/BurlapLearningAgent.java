package edu.brown.cs.atari_vision.spaceinvaders;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.singleagent.SADomain;
import burlap.visualizer.Visualizer;

/**
 * Created by MelRod on 4/21/16.
 */
public class BurlapLearningAgent extends BurlapAgent {

    LearningAgent learningAgent;

    public BurlapLearningAgent(LearningAgent learningAgent, SADomain domain, ALEStateGenerator stateGenerator, Visualizer vis, String rom, boolean useGUI) {
        super(null, domain, stateGenerator, vis, rom, useGUI);

        this.learningAgent = learningAgent;
    }

    @Override
    public void runEpisode() {
        learningAgent.runLearningEpisode(this.env);
    }
}
