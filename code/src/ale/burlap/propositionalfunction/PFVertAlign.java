package ale.burlap.propositionalfunction;

import ale.burlap.ALEDomainConstants;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

/**
 * Created by MelRod on 3/12/16.
 */
public class PFVertAlign extends PropositionalFunction {

    int width;

    public PFVertAlign(String name, Domain domain, String[] parameterClasses, int width) {
        super(name, domain, parameterClasses);

        this.width = width;
    }

    @Override
    public boolean isTrue(State s, String... params) {

        ObjectInstance objectA = s.getObject(params[0]);
        ObjectInstance objectB = s.getObject(params[1]);

        if (width/2 >= Math.abs(objectA.getIntValForAttribute(ALEDomainConstants.XATTNAME)
                        - objectB.getIntValForAttribute(ALEDomainConstants.XATTNAME))) {
            return true;
        } else {
            return false;
        }
    }
}
