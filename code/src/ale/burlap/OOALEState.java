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

        return s;
    }
}
