package ale.burlap;

import ale.screen.ScreenMatrix;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;

/**
 * Created by MelRod on 3/18/16.
 */
public interface ALEState extends State {

    public ALEState updateStateWithScreen(Domain domain, ScreenMatrix newScreen);
}
