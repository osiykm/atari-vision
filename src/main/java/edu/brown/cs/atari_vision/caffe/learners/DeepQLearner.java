package edu.brown.cs.atari_vision.caffe.learners;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import edu.brown.cs.atari_vision.caffe.exampledomains.NNGridWorld;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;

import java.util.List;

/**
 * Created by MelRod on 5/24/16.
 */
public class DeepQLearner extends ApproximateQLearning {

    public DeepQLearner(SADomain domain, double gamma, Policy policy, NNVFA vfa) {
        super(domain, gamma, vfa);

        // Finds backup using previous parameters
        this.useStaleTarget(10000);

        setLearningPolicy(policy);
    }

    @Override
    public void updateQFunction(List<EnvironmentOutcome> samples) {
        ((NNVFA)vfa).updateQFunction(samples, (NNVFA)staleVfa);
    }

    @Override
    public void updateStaleFunction() {
        if (this.staleDuration > 1) {
            ((NNVFA)this.staleVfa).updateParamsToMatch((NNVFA)this.vfa);
        } else {
            this.staleVfa = this.vfa;
        }
        this.stepsSinceStale = 1;
    }
}
