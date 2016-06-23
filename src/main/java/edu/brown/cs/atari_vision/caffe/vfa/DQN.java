package edu.brown.cs.atari_vision.caffe.vfa;

import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.caffe.nnstate.NNState;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Loader;

import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by MelRod on 5/24/16.
 */
public class DQN extends NNVFA {

    static final String SOLVER_FILE = "dqn_solver.prototxt";

    static final String INPUT_NAME = "frames_input_layer";
    static final String FILTER_NAME = "filter_input_layer";
    static final String YS_NAME = "target_input_layer";

    static final int GPU_DEVICE_ID = 0;

    public static final String Q_VALUES_BLOB_NAME = "q_values";

    public DQN(ActionSet actionSet, double gamma) {
        super(actionSet, gamma);
        constructNetwork();
    }

    protected DQN(DQN dqn) {
        super(dqn);

        constructNetwork();
        updateParamsToMatch(dqn);
    }

    @Override
    protected void constructNetwork() {

        SolverParameter solver_param = new SolverParameter();
        ReadProtoFromTextFileOrDie(SOLVER_FILE, solver_param);

        if (solver_param.solver_mode() == SolverParameter_SolverMode_GPU) {
            Caffe.set_mode(Caffe.GPU);
//            Caffe.SetDevice(GPU_DEVICE_ID);
        } else {
            Caffe.set_mode(Caffe.CPU);
        }

        this.caffeSolver = FloatSolverRegistry.CreateSolver(solver_param);
        this.caffeNet = caffeSolver.net();

        this.inputLayer = new FloatMemoryDataLayer(caffeNet.layer_by_name(INPUT_NAME));
        this.filterLayer = new FloatMemoryDataLayer(caffeNet.layer_by_name(FILTER_NAME));
        this.yLayer = new FloatMemoryDataLayer(caffeNet.layer_by_name(YS_NAME));

        this.primeStateInputs = (new FloatPointer(BATCH_SIZE * inputSize())).fill(0);
        this.stateInputs = (new FloatPointer(BATCH_SIZE * inputSize())).fill(0);
        this.dummyInputData = (new FloatPointer(BATCH_SIZE * inputSize())).fill(0);

        this.qValuesBlob = caffeNet.blob_by_name(Q_VALUES_BLOB_NAME);
    }

    @Override
    protected void convertStateToInput(State state, FloatPointer input) {
        ((NNState)state).getInput(input);
    }

    @Override
    protected int inputSize() {
        return 84*84*4;
    }

    @Override
    public ParametricStateActionFunction copy() {
        return new DQN(this);
    }
}
