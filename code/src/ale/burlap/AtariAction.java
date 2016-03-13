package ale.burlap;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.SimpleAction;

/**
 * Created by MelRod on 3/12/16.
 */
public class AtariAction extends SimpleAction {

    public AtariAction(String name, Domain domain) {
        super(name, domain);
    }

    @Override
    protected State performActionHelper(State s, GroundedAction groundedAction) {
        return s;
    }
}
