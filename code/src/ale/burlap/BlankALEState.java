package ale.burlap;

import ale.screen.ScreenMatrix;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by MelRod on 3/18/16.
 */
public class BlankALEState implements ALEState {

    private ScreenMatrix screen;

    public BlankALEState() {}
    public BlankALEState(ScreenMatrix screen) {
        this.screen = screen;
    }
    public BlankALEState(BlankALEState state) {
        this.screen = (ScreenMatrix)state.screen.clone();
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, ScreenMatrix newScreen) {
        return new BlankALEState(newScreen);
    }

    @Override
    public State copy() {
        return new BlankALEState(this);
    }

    public ScreenMatrix getScreen() {
        return screen;
    }


    /** not implemented State methods **/

    @Override
    public State addObject(ObjectInstance o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State addAllObjects(Collection<ObjectInstance> objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State removeObject(String oname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> State setObjectsValue(String objectName, String attName, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State removeObject(ObjectInstance o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State removeAllObjects(Collection<ObjectInstance> objects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State renameObject(String originalName, String newName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public State renameObject(ObjectInstance o, String newName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getObjectMatchingTo(State so, boolean enforceStateExactness) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int numTotalObjects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInstance getObject(String oname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ObjectInstance> getAllObjects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ObjectInstance> getObjectsOfClass(String oclass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectInstance getFirstObjectOfClass(String oclass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getObjectClassesPresent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<ObjectInstance>> getAllObjectsByClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCompleteStateDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, List<String>> getAllUnsetAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCompleteStateDescriptionWithUnsetAttributesAsNull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<List<String>> getPossibleBindingsGivenParamOrderGroups(String[] paramClasses, String[] paramOrderGroups) {
        throw new UnsupportedOperationException();
    }
}
