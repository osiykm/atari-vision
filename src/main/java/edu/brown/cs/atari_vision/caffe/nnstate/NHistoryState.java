package edu.brown.cs.atari_vision.caffe.nnstate;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.ALEState;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;
import static org.bytedeco.javacpp.opencv_core.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by MelRod on 5/27/16.
 */
public class NHistoryState implements NNState, ALEState {

    static BytePointer frameHistory;
    static PreProcessor preProcessor;
    static int n; // the history size

    protected long index;

    protected NHistoryState(long index) {
        this.index = index;
    }

    public NHistoryState(int n, int totalHistorySize, PreProcessor preProcessor) {
        NHistoryState.n = n;
        NHistoryState.preProcessor = preProcessor;

        int outputSize = preProcessor.outputSize();

        // Create the frame history data size to be totalHistorySize + a padding on both sides of n - 1
        int paddingSize = (n - 1) * outputSize;
        NHistoryState.frameHistory = (new BytePointer(totalHistorySize * outputSize + 2 * paddingSize)).zero();

        this.index = paddingSize + outputSize;
    }

    @Override
    public FloatPointer getInput() {

        int outputSize = preProcessor.outputSize();

        FloatPointer input = preProcessor.convertDataToInput(frameHistory.position(index - (n-1)*outputSize), n);

        return input;
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, Mat newScreen) {

        int outputSize = preProcessor.outputSize();
        long frameHistoryDataSize = frameHistory.capacity();
        long paddingSize = (n - 1) * outputSize;

        // Find new index
        long newIndex = index + outputSize;
        if (newIndex >= frameHistoryDataSize) {
            // Copy the buffer to the start of the history
            BytePointer frameHistoryCopy = new BytePointer(frameHistory);
            frameHistory.position(0).limit(paddingSize).put(frameHistoryCopy.position(frameHistoryDataSize - paddingSize));
            frameHistory.limit(frameHistory.capacity());

            newIndex = paddingSize;
        }

        // Create new state
        NHistoryState newState = new NHistoryState(newIndex);

        // Process the new screen
        BytePointer newData = preProcessor.convertScreenToData(newScreen);

        // Place data in history
        frameHistory.position(newIndex).put(newData.limit(outputSize));

//        print2D(frameHistory.position(0), 7, 3, 1);

        return newState;
    }

    // DEBUG
    public static void print2D(BytePointer ptr, int rows, int cols, int n) {

        ByteBuffer buffer = ptr.position(0).limit(ptr.capacity()).asBuffer();

        for (int i = 0; i < n; i++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    System.out.print(String.format("%d ", buffer.get()));
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    public List<Object> variableKeys() {
        return null;
    }

    @Override
    public Object get(Object variableKey) {
        return null;
    }

    @Override
    public State copy() {
        NHistoryState newState = new NHistoryState(this.index);
        return newState;
    }
}