package edu.brown.cs.atari_vision.caffe.experiencereplay;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import edu.brown.cs.atari_vision.ale.burlap.ALEState;
import edu.brown.cs.atari_vision.caffe.nnstate.NNState;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.opencv_core.*;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by MelRod on 5/27/16.
 */
public class FrameHistoryState implements NNState, ALEState {

    public long index;
    public int historyLength;
    public FrameExperienceMemory memory;


    FrameHistoryState(FrameExperienceMemory memory, long index, int historyLength) {
        this.memory = memory;

        this.index = index;
        this.historyLength = historyLength;
    }

    public FrameHistoryState(FrameExperienceMemory memory) {
        this.memory = memory;

        this.index = memory.currentFrameIndex;
        this.historyLength = 0;
    }

    @Override
    public FloatPointer getInput() {

        long frameSize = memory.frameSize();

        FloatPointer processedFrame = memory.preProcessor.convertDataToInput(
                memory.frameHistory.position(index - (historyLength - 1)*frameSize),
                historyLength);

        FloatPointer input;
        if (historyLength < memory.n) {
            input = new FloatPointer(frameSize * memory.n);
            if (historyLength > 0) {
                input.position((memory.n - historyLength)*frameSize).put(processedFrame.limit(historyLength * frameSize));
                input.position(0).limit((memory.n - historyLength)*frameSize).zero();
                input.limit(memory.n * frameSize);
            } else {
                input.zero();
            }
        } else {
            input = processedFrame;
        }


        return input;
    }

    @Override
    public ALEState updateStateWithScreen(Domain domain, Mat newScreen) {
        return this.memory.addNewFrame(this, newScreen);
    }

    @Override
    public ALEState reset() {
        historyLength = 0;
        return this;
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
        FrameHistoryState newState = new FrameHistoryState(this.memory, this.index, this.historyLength);
        return newState;
    }
}