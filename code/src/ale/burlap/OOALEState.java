package ale.burlap;

import ale.cv.Sprite;
import ale.cv.SpriteFinder;
import ale.screen.ScreenMatrix;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import org.opencv.core.Point;

import java.util.ArrayList;
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

        Map<Sprite, ArrayList<Point>> spritePoints = SpriteFinder.getSpriteFinder().findSprites(newScreen);

        for (Map.Entry<Sprite, ArrayList<Point>> entry : spritePoints.entrySet()) {
            Sprite sprite = entry.getKey();
            ArrayList<Point> points = entry.getValue();

            String spriteId = sprite.getId();
            for (int i = 0; i < points.size(); i++) {
                Point point = points.get(i);
                ObjectInstance o = new MutableObjectInstance(domain.getObjectClass(spriteId), spriteId+point.x+point.y);

                o.setValue(ALEDomainConstants.XATTNAME, point.x);
                o.setValue(ALEDomainConstants.YATTNAME, point.y);

                s.addObject(o);
            }
        }

//        System.err.println("---------------------------------------------------------------------------------");
//        for (ObjectInstance o : s.getAllObjects()) {
//            System.err.println(o.getName() + ": " + o.getValueForAttribute(ALEDomainConstants.XATTNAME) + ", " + o.getValueForAttribute(ALEDomainConstants.YATTNAME));
//        }

        return s;
    }
}
