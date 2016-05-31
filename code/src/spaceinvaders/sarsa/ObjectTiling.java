package spaceinvaders.sarsa;

import ale.io.Actions;
import burlap.behavior.singleagent.vfa.DifferentiableStateActionValue;
import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

/**
 * Created by MelRod on 4/28/16.
 */
public class ObjectTiling implements DifferentiableStateActionValue, Serializable {

    public BitSet bitSet;
    public double[] params;

    public String objectClass;
    public String xAttributeName;
    public String yAttributeName;

    public int minX;
    public int minY;

    public int numTilesX;
    public int numTilesY;
    public int numTiles;
    public int numParams;

    public float tileWidth;
    public float tileHeight;

    public ObjectTiling(String objectClass, String xAttributeName, String yAttributeName, int minX, int minY, int maxX, int maxY, int numTilesX, int numTilesY) {
        this.objectClass = objectClass;
        this.xAttributeName = xAttributeName;
        this.yAttributeName = yAttributeName;

        this.minX = minX;
        this.minY = minY;

        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;

        float width = maxX - minX;
        float height = maxY - minY;
        this.tileWidth = width/numTilesX;
        this.tileHeight = height/numTilesY;
        this.numTiles = numTilesX * numTilesY;

        this.bitSet = new BitSet(numTiles);

        this.numParams = numTiles * Actions.numPlayerActions;
        this.params = new double[numParams];
    }

    public ObjectTiling(String objectClass, int numParams, int numTilesX, int numTilesY, float tileWidth, float tileHeight) {
        this.objectClass = objectClass;

        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;

        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.numTiles = numTilesX * numTilesY;

        this.bitSet = new BitSet(numTiles);

        this.numParams = numParams;
        this.params = new double[numParams];
    }

    public void setBitSetForState(State s) {

        bitSet.clear();

        List<ObjectInstance> objects = s.getObjectsOfClass(objectClass);

        for (ObjectInstance object : objects) {
            int x = object.getIntValForAttribute(xAttributeName) - minX;
            int y = object.getIntValForAttribute(yAttributeName) - minY;

            int xTile = (int)((float)x/tileWidth);
            int yTile = (int)((float)y/tileHeight);

            int bitIndex = (yTile*numTilesX) + xTile;
            bitSet.set(bitIndex);
        }
    }

    @Override
    public FunctionGradient gradient(State state, AbstractGroundedAction abstractGroundedAction) {
        setBitSetForState(state);

        int action = Actions.map(abstractGroundedAction.actionName());

        int indexOffset = action * numTiles;

        FunctionGradient.SparseGradient gradient = new FunctionGradient.SparseGradient(numParams);
        for (int i = 0; i < numTiles; i++) {
            if (bitSet.get(i)) {
                gradient.put(indexOffset + i, 1);
            }
        }

        return gradient;
    }

//    public void saveToFile(String fileName) throws IOException {
//        FileOutputStream fos;
//        fos = new FileOutputStream(fileName);
//
//        DataOutputStream out = new DataOutputStream(fos);
//
//        for (int i = 0; i < numParams; i ++) {
//            out.writeDouble(params[i]);
//        }
//    }

    @Override
    public double evaluate(State state, AbstractGroundedAction abstractGroundedAction) {
        setBitSetForState(state);

        int action = Actions.map(abstractGroundedAction.actionName());

        int indexOffset = action * numTiles;

        double sum = 0;
        for (int i = 0; i < numTiles; i++) {
            if (bitSet.get(i)) {
                sum += params[i + indexOffset];
            }
        }
        return sum;
    }

    @Override
    public int numParameters() {
        return numParams;
    }

    @Override
    public double getParameter(int i) {
        return params[i];
    }

    @Override
    public void setParameter(int i, double v) {
        params[i] = v;
    }

    @Override
    public void resetParameters() {
        for (int i = 0; i < numParams; i++) {
            params[i] = 0;
        }
    }

    @Override
    public ParametricFunction copy() {
        return new ObjectTiling(objectClass, numParams, numTilesX, numTilesY, tileWidth, tileHeight);
    }
}
