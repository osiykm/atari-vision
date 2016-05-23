package ale.burlap.alepolicies;

import ale.burlap.ALEDomainConstants;
import burlap.behavior.policy.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.common.NullAction;

import java.util.List;

/**
 * Created by MelRod on 3/22/16.
 */
public class NaiveSIPolicy extends Policy {

    /**
     * The actions from which selection is performed
     */
    private Action rightAction;
    private Action rightFireAction;
    private Action leftAction;
    private Action leftFireAction;

    private PropositionalFunction vertAlignPF;

    private boolean movingRight;

    public NaiveSIPolicy(Domain domain) {
        movingRight = true;

        rightAction = new NullAction("player_a_right");
        rightFireAction = new NullAction("player_a_rightfire");
        leftAction = new NullAction("player_a_left");
        leftFireAction = new NullAction("player_a_leftfire");

        vertAlignPF = domain.getPropFunction(ALEDomainConstants.PFVertAlign);
    }

    @Override
    public AbstractGroundedAction getAction(State s) {

        List <ObjectInstance> instances = s.getObjectsOfClass(ALEDomainConstants.CLASSAGENT);

        if (instances.size() > 0) {
            ObjectInstance agent = instances.get(0);

            int x = agent.getIntValForAttribute(ALEDomainConstants.XATTNAME);

            if (movingRight && x >= 115) {
                movingRight = false;
            } else if (!movingRight && x <= 40) {
                movingRight = true;
            }
        }

        // check whether or not there is an alien overhead
        boolean hasVertAlign = false;
        List<GroundedProp> gps = vertAlignPF.getAllGroundedPropsForState(s);
        for(GroundedProp gp : gps) {
            if (gp.isTrue(s)) {
                hasVertAlign = true;
                break;
            }
        }

        // execute naive action
        if (hasVertAlign) {
            if (movingRight) {
                return rightFireAction.getGroundedAction();
            } else {
                return leftFireAction.getGroundedAction();
            }
        } else {
            if (movingRight) {
                return rightAction.getGroundedAction();
            } else {
                return leftAction.getGroundedAction();
            }
        }
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State s) {
        return null;
    }

    @Override
    public boolean isStochastic() {
        return false;
    }

    @Override
    public boolean isDefinedFor(State s) {
        return true;
    }
}
