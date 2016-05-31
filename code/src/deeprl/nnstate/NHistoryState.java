package deeprl.nnstate;

import ale.burlap.ALEState;
import ale.screen.ScreenMatrix;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import deeprl.preprocess.PreProcessor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

/**
 * Created by MelRod on 5/27/16.
 */
public class NHistoryState implements NNState {

    private List<INDArray> frameHistory;
    private PreProcessor preProcessor;
    private int n; // the history size

    protected NHistoryState(NHistoryState oldState) {
        this.n = oldState.n;
        this.preProcessor = oldState.preProcessor;
    }

    public NHistoryState(int n, PreProcessor preProcessor) {
        this.n = n;
        this.preProcessor = preProcessor;
        int frameSize = preProcessor.inputSize();

        this.frameHistory = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.frameHistory.add(Nd4j.zeros(frameSize));
        }
    }

    @Override
    public INDArray getInput() {
        return Nd4j.hstack(frameHistory.toArray(new INDArray[n]));
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, ScreenMatrix newScreen) {

        // Create new input list
        List<INDArray> newFrameHistory = new ArrayList<>(n);

        // Process the new input
        newFrameHistory.add(preProcessor.convertScreenToInput(newScreen));

        // Add history
        for (int i = 0; i < n - 1; i++) {
            newFrameHistory.add(frameHistory.get(i));
        }

        // Create new state
        NHistoryState newState = new NHistoryState(this);
        newState.frameHistory = newFrameHistory;

        return newState;
    }

    @Override
    public State copy() {
        NHistoryState newState = new NHistoryState(this);
        newState.frameHistory = frameHistory;
        return newState;
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