package ale.burlap;

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
public class SIALEState extends MutableState implements ALEState {

    public static final int X_BOMB_STITCH_THRESHOLD = 1;
    public static final int Y_BOMB_STITCH_THRESHOLD = 5;

    ScreenMatrix screen;

    @Override
    public ALEState updateStateWithScreen(Domain domain, ScreenMatrix newScreen) {

        SIALEState s = new SIALEState();
        s.screen = newScreen;

        // Find sprites using TargetedContourFinder
        Map<String, List<Point>> sprites = TargetedContourFinder.getTCF().findSprites(newScreen);

        // add agent
        List<Point> agentLocs = sprites.get(ALEDomainConstants.CLASSAGENT);
        Point agentLoc;
        if (agentLocs.size() > 0) {
            agentLoc = agentLocs.get(0);
            ObjectInstance obj = new MutableObjectInstance(domain.getObjectClass(ALEDomainConstants.CLASSAGENT),
                    String.format("AG:%f,%f", agentLoc.x, agentLoc.y));

            obj.setValue(ALEDomainConstants.XATTNAME, agentLoc.x);
            obj.setValue(ALEDomainConstants.YATTNAME, agentLoc.y);
            s.addObject(obj);
        } else {
            // the agent wasn't found
            agentLoc = new Point(0,0);
        }


        // add aliens
        for (Point point : sprites.get(ALEDomainConstants.CLASSALIEN)) {
            ObjectInstance obj = new MutableObjectInstance(domain.getObjectClass(ALEDomainConstants.CLASSALIEN),
                    String.format("AL:%f,%f", point.x, point.y));

            obj.setValue(ALEDomainConstants.XATTNAME, point.x);
            obj.setValue(ALEDomainConstants.YATTNAME, point.y);

            obj.setValue(ALEDomainConstants.AGENT_CENT_XATTNAME, point.x - agentLoc.x);
            obj.setValue(ALEDomainConstants.AGENT_CENT_YATTNAME, point.y - agentLoc.y);

            s.addObject(obj);
        }


        // add bombs
        List<ObjectInstance> oldBombs = new ArrayList<>();
        oldBombs.addAll(this.getObjectsOfClass(ALEDomainConstants.CLASS_BOMB_UNKNOWN));
        oldBombs.addAll(this.getObjectsOfClass(ALEDomainConstants.CLASS_BOMB_AGENT));
        oldBombs.addAll(this.getObjectsOfClass(ALEDomainConstants.CLASS_BOMB_ALIEN));

        for (Point point : sprites.get(ALEDomainConstants.CLASS_BOMB_UNKNOWN)) {
            ObjectInstance oldBomb = bombWithinThreshold(point, oldBombs);
            String bombClass;
            if (oldBomb == null) {
                bombClass = ALEDomainConstants.CLASS_BOMB_UNKNOWN;
            } else {

                String oldBombClass = oldBomb.getClassName();
                if (oldBombClass.equals(ALEDomainConstants.CLASS_BOMB_UNKNOWN)) {
                    // This bomb needs to be classified
                    int deltaY = (int)(point.y - oldBomb.getIntValForAttribute(ALEDomainConstants.YATTNAME));
                    if (deltaY > 0) {
                        bombClass = ALEDomainConstants.CLASS_BOMB_ALIEN;
                    } else if (deltaY < 0) {
                        bombClass = ALEDomainConstants.CLASS_BOMB_AGENT;
                    } else {
                        bombClass = ALEDomainConstants.CLASS_BOMB_UNKNOWN;
                    }
                } else {
                    bombClass = oldBombClass;
                }
            }

            ObjectInstance obj = new MutableObjectInstance(domain.getObjectClass(bombClass),
                    String.format("B:%f,%f", point.x, point.y));

            obj.setValue(ALEDomainConstants.XATTNAME, point.x);
            obj.setValue(ALEDomainConstants.YATTNAME, point.y);

            obj.setValue(ALEDomainConstants.AGENT_CENT_XATTNAME, point.x - agentLoc.x);
            obj.setValue(ALEDomainConstants.AGENT_CENT_YATTNAME, point.y - agentLoc.y);

            s.addObject(obj);
        }


        return s;
    }

    public ObjectInstance bombWithinThreshold(Point point, List<ObjectInstance> bombs) {
        ObjectInstance oldBomb = null;

        for (ObjectInstance bomb : bombs) {
            int x = bomb.getIntValForAttribute(ALEDomainConstants.XATTNAME);
            int y = bomb.getIntValForAttribute(ALEDomainConstants.YATTNAME);
            if (Math.abs(x - point.x) <= X_BOMB_STITCH_THRESHOLD) {
                if (Math.abs(y - point.y) <= Y_BOMB_STITCH_THRESHOLD) {
                    if (oldBomb == null) {
                        oldBomb = bomb;
                    } else {
                        // we found multiple matching bombs, so we don't know which one to stitch
                        return null;
                    }
                }
            }
        }

        return oldBomb;
    }
}
