package ale.burlap;

import ale.io.Actions;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.NullAction;

/**
 * Created by MelRod on 3/18/16.
 */
public class ALEDomainGenerator implements DomainGenerator {
    @Override
    public Domain generateDomain() {
        Domain domain = new SADomain();

        // add in NullActions for Domain
        for (int i = 0 ; i < Actions.numPlayerActions ; i++) {
            new NullAction(Actions.actionNames[i], domain);
        }

        return domain;
    }
}
