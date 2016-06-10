package edu.brown.cs.atari_vision.caffe.vfa;

import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.caffe.nnstate.NNState;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by MelRod on 5/24/16.
 */
public class DQN extends NNVFA {

    static final String SOLVER_FILE = "dqn_solver.prototxt";

    static final String INPUT_NAME = "frames_input_layer";
    static final String FILTER_NAME = "filter_input_layer";
    static final String YS_NAME = "target_input_layer";

    public DQN(ActionSet actionSet, double gamma) {
        super(actionSet, gamma);
        constructNetwork();
    }

    protected DQN(DQN dqn) {
        super(dqn);
    }

    @Override
    protected void constructNetwork() {

        SolverParameter solver_param = new SolverParameter();
        ReadProtoFromTextFileOrDie(SOLVER_FILE, solver_param);

        if (solver_param.solver_mode() == SolverParameter_SolverMode_GPU) {
            Caffe.set_mode(Caffe.GPU);
        }

        FloatSolver solver = FloatSolverRegistry.CreateSolver(solver_param);
        this.caffeNet = solver.net();

        this.inputLayer = (FloatMemoryDataLayer)caffeNet.layer_by_name(INPUT_NAME);
        this.filterLayer = (FloatMemoryDataLayer)caffeNet.layer_by_name(FILTER_NAME);
        this.yLayer = (FloatMemoryDataLayer)caffeNet.layer_by_name(YS_NAME);
    }

    @Override
    protected FloatBlob convertStateToInput(State state) {
        return ((NNState)state).getInput();
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
