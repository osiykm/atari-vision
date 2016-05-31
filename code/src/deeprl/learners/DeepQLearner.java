package deeprl.learners;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import deeprl.vfa.NNVFA;

import java.util.List;

/**
 * Created by MelRod on 5/24/16.
 */
public class DeepQLearner extends ApproximateQLearning {

    public DeepQLearner(Domain domain, double gamma, Policy policy, NNVFA vfa) {
        super(domain, gamma, vfa);

        // Finds backup using previous parameters
        this.useStaleTarget(2);

        setLearningPolicy(policy);
    }

    @Override
    public void updateQFunction(List<EnvironmentOutcome> samples) {
        ((NNVFA)vfa).updateQFunction(samples, (NNVFA)staleVfa);
    }
}
