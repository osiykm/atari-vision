package edu.brown.cs.atari_vision.caffe.training;

import burlap.behavior.policy.Policy;
import burlap.mdp.singleagent.environment.Environment;
import edu.brown.cs.atari_vision.caffe.action.ActionSet;
import edu.brown.cs.atari_vision.caffe.learners.DeepQLearner;
import edu.brown.cs.atari_vision.caffe.vfa.DQN;

/**
 * Created by maroderi on 6/22/16.
 */
public class SimpleTrainer extends TrainingHelper {

    public SimpleTrainer(DeepQLearner learner, DQN vfa, Policy testPolicy, ActionSet actionSet, Environment env) {
        super(learner, vfa, testPolicy, actionSet, env);
    }

    @Override
    public void prepareForTraining() {

    }

    @Override
    public void prepareForTesting() {

    }
}
