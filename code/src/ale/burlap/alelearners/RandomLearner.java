package ale.burlap.alelearners;

import ale.io.Actions;
import burlap.behavior.policy.RandomPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

/**
 * Created by MelRod on 3/18/16.
 */
public class RandomLearner implements LearningAgent {

    RandomPolicy policy;
    private int eStepCounter;

    public RandomLearner(Domain domain) {
        policy = new RandomPolicy(domain);
    }

    @Override
    public EpisodeAnalysis runLearningEpisode(Environment env) {
        return runLearningEpisode(env, -1);
    }

    @Override
    public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {
        EpisodeAnalysis ea = new EpisodeAnalysis(env.getCurrentObservation());
        eStepCounter = 0;

        while(!env.isInTerminalState() && (eStepCounter < maxSteps || maxSteps == -1)){
            State curState = env.getCurrentObservation();
            GroundedAction action = (GroundedAction)policy.getAction(curState);

            EnvironmentOutcome eo = action.executeIn(env);
            ea.recordTransitionTo(eo.a, eo.o, eo.r);
        }

        return ea;
    }
}
