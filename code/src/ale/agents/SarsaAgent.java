package ale.agents;

import ale.burlap.AtariEnvironment;
import ale.burlap.DomainConstants;
import ale.burlap.SIDomainGenerator;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.vfa.DifferentiableStateActionValue;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase;
import burlap.domain.singleagent.lunarlander.LunarLanderDomain;
import burlap.oomdp.core.Domain;

/**
 * Created by MelRod on 3/13/16.
 */
public class SarsaAgent extends BURLAPAgent {

    public SarsaAgent(boolean useGUI) {
        super(useGUI);
    }

    @Override
    public LearningAgent generateLearningAgent() {

        int nTilings = 5;
        CMACFeatureDatabase cmac = new CMACFeatureDatabase(nTilings,
                CMACFeatureDatabase.TilingArrangement.RANDOMJITTER);
        double resolution = 10.;

        double xWidth = SIDomainGenerator.objectWidth / resolution;
        double yWidth = SIDomainGenerator.objectWidth / resolution;

        // agent tiling
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSAGENT,
                domain.getAttribute(DomainConstants.XATTNAME),
                xWidth);
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSAGENT,
                domain.getAttribute(DomainConstants.YATTNAME),
                yWidth);

        // alien tiling
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSALIEN,
                domain.getAttribute(DomainConstants.XATTNAME),
                xWidth);
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSALIEN,
                domain.getAttribute(DomainConstants.YATTNAME),
                yWidth);

        // bomb tiling
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSBOMB,
                domain.getAttribute(DomainConstants.XATTNAME),
                xWidth);
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSBOMB,
                domain.getAttribute(DomainConstants.YATTNAME),
                yWidth);
        cmac.addSpecificationForAllTilings(DomainConstants.CLASSBOMB,
                domain.getAttribute(DomainConstants.VYATTNAME),
                1);

        double defaultQ = 0.5;
        DifferentiableStateActionValue vfa = cmac.generateVFA(defaultQ/nTilings);

        return new GradientDescentSarsaLam(this.domain, 0.99, vfa, 0.02, 0.5);
    }
}
