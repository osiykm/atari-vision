package ale.burlap;

import ale.burlap.propositionalfunction.PFVertAlign;
import ale.io.Actions;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.SADomain;

/**
 * Space Invaders Domain Generator
 *
 * Created by MelRod on 3/12/16.
 */
public class SIDomainGenerator implements DomainGenerator {

    public static int objectWidth = 10;


    @Override
    public Domain generateDomain() {
        Domain domain = new SADomain();

        // Attributes
        Attribute xAtt = new Attribute(domain, DomainConstants.XATTNAME, Attribute.AttributeType.INT);
        Attribute yAtt = new Attribute(domain, DomainConstants.YATTNAME, Attribute.AttributeType.INT);
        Attribute vxAtt = new Attribute(domain, DomainConstants.VXATTNAME, Attribute.AttributeType.INT);
        Attribute vyAtt = new Attribute(domain, DomainConstants.VYATTNAME, Attribute.AttributeType.INT);

        // Object classes
        // agent
        ObjectClass agentClass = new ObjectClass(domain, DomainConstants.CLASSAGENT);
        agentClass.addAttribute(xAtt);
        agentClass.addAttribute(yAtt);
        agentClass.addAttribute(vxAtt);
        // alien
        ObjectClass alienClass = new ObjectClass(domain, DomainConstants.CLASSALIEN);
        alienClass.addAttribute(xAtt);
        alienClass.addAttribute(yAtt);
        alienClass.addAttribute(vxAtt);
        // bomb
        ObjectClass bombClass = new ObjectClass(domain, DomainConstants.CLASSBOMB);
        bombClass.addAttribute(xAtt);
        bombClass.addAttribute(yAtt);
        bombClass.addAttribute(vyAtt);

        // Actions
        for (int i = 0 ; i < Actions.numPlayerActions ; i++) {
            new AtariAction(Actions.actionNames[i], domain);
        }

        // Propositional Functions
        new PFVertAlign(
                DomainConstants.PFVertAlign,
                domain,
                new String[]{DomainConstants.CLASSAGENT, DomainConstants.CLASSALIEN},
                objectWidth);
        new PFVertAlign(
                DomainConstants.PFVertAlign,
                domain,
                new String[]{DomainConstants.CLASSAGENT, DomainConstants.CLASSBOMB},
                objectWidth);

        return domain;
    }
}
