package ale.burlap;

import ale.cv.Sprite;
import ale.cv.SpriteFinder;
import ale.cv.TargetedContourFinder;
import ale.screen.ScreenMatrix;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by MelRod on 3/18/16.
 */
public class OOALEState extends MutableState implements ALEState {

    ScreenMatrix screen;

    @Override
    public ALEState updateStateWithScreen(Domain domain, ScreenMatrix newScreen) {

        OOALEState s = new OOALEState();
        s.screen = newScreen;

        // Find sprites using TargetedContourFinder
        List<List<Point>> sprites = TargetedContourFinder.getTCF().findSprites(newScreen);
        for (int i = 0; i < sprites.size(); i++) {
            String spriteID = TargetedContourFinder.CLASS_IDS[i];
            for (Point point : sprites.get(i)) {
                ObjectInstance obj = new MutableObjectInstance(domain.getObjectClass(spriteID),
                        spriteID + point.x + point.y);
                obj.setValue(ALEDomainConstants.XATTNAME, point.x);
                obj.setValue(ALEDomainConstants.YATTNAME, point.y);
                s.addObject(obj);
            }
        }

        // Set agent-centered attributes
        List<ObjectInstance> agentInstances = s.getObjectsOfClass(ALEDomainConstants.CLASSAGENT);
        int agentX;
        int agentY;
        if (agentInstances.size() > 0) {
            ObjectInstance agentInstance = agentInstances.get(0);
            agentX = agentInstance.getIntValForAttribute(ALEDomainConstants.XATTNAME);
            agentY = agentInstance.getIntValForAttribute(ALEDomainConstants.YATTNAME);
        } else {
            agentX = 0;
            agentY = 0;
        }
        for (int i = 0; i < sprites.size(); i++) {
            String spriteID = TargetedContourFinder.CLASS_IDS[i];

            if (spriteID.equals(ALEDomainConstants.CLASSAGENT)) {
                continue;
            }

            for (ObjectInstance obj : s.getObjectsOfClass(spriteID)) {

                int x = obj.getIntValForAttribute(ALEDomainConstants.XATTNAME);
                int y = obj.getIntValForAttribute(ALEDomainConstants.YATTNAME);

                obj.setValue(ALEDomainConstants.AGENT_CENT_XATTNAME, x - agentX);
                obj.setValue(ALEDomainConstants.AGENT_CENT_YATTNAME, y - agentY);
            }
        }


        return s;
    }
}
