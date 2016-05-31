package deeprl.policies;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.valuefunction.QFunction;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

/**
 * Created by MelRod on 5/4/16.
 */
public class AnnealedEpsilonGreedy extends EpsilonGreedy {

    protected double epsilonStart;
    protected double epsilonEnd;
    protected double epsilonStep;
    protected int annealingTime;


    public AnnealedEpsilonGreedy(QFunction planner, double epsilonStart, double epsilonEnd, int annealingTime) {
        super(planner, epsilonStart);

        this.epsilonStart = epsilonStart;
        this.epsilonEnd = epsilonEnd;
        this.epsilonStep = (epsilonEnd - epsilonStart)/annealingTime;
        this.annealingTime = annealingTime;
    }

    @Override
    public AbstractGroundedAction getAction(State s) {
        AbstractGroundedAction action = super.getAction(s);

        if (epsilon > epsilonEnd) {
            epsilon += epsilonStep;

            if (epsilon < epsilonEnd) {
                epsilon = epsilonEnd;
            }
        }

        double test = epsilon%0.1;
        if (test <= -epsilonStep && epsilon > epsilonEnd) {
            System.out.println("EPSILON: " + epsilon);
        }

        return action;
    }
}
