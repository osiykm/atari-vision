package edu.brown.cs.atari_vision.spaceinvaders;


import burlap.mdp.core.Domain;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainConstants;
import edu.brown.cs.atari_vision.ale.burlap.ALEState;
import edu.brown.cs.atari_vision.spaceinvaders.cv.TargetedContourFinder;
import edu.brown.cs.atari_vision.spaceinvaders.objects.AgentObject;
import edu.brown.cs.atari_vision.spaceinvaders.objects.AlienObject;
import edu.brown.cs.atari_vision.spaceinvaders.objects.BombObject;

import org.bytedeco.javacpp.opencv_core.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by MelRod on 3/18/16.
 */
public class SIALEState extends GenericOOState implements ALEState {

    public static final int X_BOMB_STITCH_THRESHOLD = 1;
    public static final int Y_BOMB_STITCH_THRESHOLD = 5;

    Mat screen;

    @Override
    public ALEState updateStateWithScreen(Domain domain, Mat newScreen) {

        SIALEState s = new SIALEState();
        s.screen = newScreen;

        // Find sprites using TargetedContourFinder
        Map<String, List<Point>> sprites = TargetedContourFinder.getTCF().findSprites(newScreen);

        // add agent
        List<Point> agentLocs = sprites.get(ALEDomainConstants.CLASSAGENT);
        AgentObject agentObject;
        if (agentLocs.size() > 0) {
            Point point = agentLocs.get(0);
            agentObject = new AgentObject((int)point.x(), (int)point.y());
            s.addObject(agentObject);
        } else {
            // the agent wasn't found
            agentObject = new AgentObject(0, 0);
        }


        // add aliens
        for (Point point : sprites.get(ALEDomainConstants.CLASSALIEN)) {
            AlienObject alienObject = new AlienObject((int)point.x(), (int)point.y(), agentObject);
            s.addObject(alienObject);
        }


        // add bombs
        List<ObjectInstance> oldBombs = new ArrayList<>();
        oldBombs.addAll(this.objectsOfClass(ALEDomainConstants.CLASS_BOMB_UNKNOWN));
        oldBombs.addAll(this.objectsOfClass(ALEDomainConstants.CLASS_BOMB_AGENT));
        oldBombs.addAll(this.objectsOfClass(ALEDomainConstants.CLASS_BOMB_ALIEN));

        for (Point point : sprites.get(ALEDomainConstants.CLASS_BOMB_UNKNOWN)) {
            ObjectInstance oldBomb = bombWithinThreshold(point, oldBombs);
            String bombClass;
            if (oldBomb == null) {
                bombClass = ALEDomainConstants.CLASS_BOMB_UNKNOWN;
            } else {

                String oldBombClass = oldBomb.className();
                if (oldBombClass.equals(ALEDomainConstants.CLASS_BOMB_UNKNOWN)) {
                    // This bomb needs to be classified
                    int deltaY = (int)(point.y() - (Integer)oldBomb.get(ALEDomainConstants.YATTNAME));
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

            BombObject bombObject = new BombObject((int)point.x(), (int)point.y(), agentObject, bombClass);
            s.addObject(bombObject);
        }

        return s;
    }

    public Mat getScreen() {
        return screen;
    }

    public ObjectInstance bombWithinThreshold(Point point, List<ObjectInstance> bombs) {
        ObjectInstance oldBomb = null;

        for (ObjectInstance bomb : bombs) {
            int x = (Integer)bomb.get(ALEDomainConstants.XATTNAME);
            int y = (Integer)bomb.get(ALEDomainConstants.YATTNAME);
            if (Math.abs(x - point.x()) <= X_BOMB_STITCH_THRESHOLD) {
                if (Math.abs(y - point.y()) <= Y_BOMB_STITCH_THRESHOLD) {
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
