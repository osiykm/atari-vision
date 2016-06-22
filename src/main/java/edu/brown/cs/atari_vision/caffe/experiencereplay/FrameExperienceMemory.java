package edu.brown.cs.atari_vision.caffe.experiencereplay;

import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import org.bytedeco.javacpp.BytePointer;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by maroderi on 6/20/16.
 */
public class FrameExperienceMemory extends FixedSizeMemory {

    BytePointer frameHistory;
    PreProcessor preProcessor;
    long currentFrameIndex;

    int totalExperienceSize;
    int n; // the history size

    public FrameExperienceMemory(int size, int maxHistoryLength, PreProcessor preProcessor) {
        super(size);
        totalExperienceSize = size;

        this.currentFrameIndex = 0;
        this.preProcessor = preProcessor;
        this.n = maxHistoryLength;

        long outputSize = preProcessor.outputSize();

        // Create the frame history data size to be totalHistorySize + a padding on both sides of n - 1
        long paddingSize = (this.n - 1) * outputSize;
        frameHistory = (new BytePointer(size * outputSize + 2 * paddingSize)).zero();
    }

    public FrameHistoryState addNewFrame(FrameHistoryState currentState, Mat newScreen) {
        if (currentState.index != currentFrameIndex) {
            throw new IllegalStateException("You can only update the most recent state");
        }

        long outputSize = preProcessor.outputSize();
        long frameHistoryDataSize = frameHistory.capacity();
        long paddingSize = (n - 1) * outputSize;

        // Find new index
        long newIndex = currentState.index + outputSize;
        if (newIndex >= frameHistoryDataSize) {
            // Copy the buffer to the start of the history
            BytePointer frameHistoryCopy = new BytePointer(frameHistory);
            frameHistory.position(0).limit(paddingSize).put(frameHistoryCopy.position(frameHistoryDataSize - paddingSize));
            frameHistory.limit(frameHistory.capacity());

            newIndex = paddingSize;
        }

        // Increment length if smaller than n
        int newHistoryLength = currentState.historyLength >= n ? n : currentState.historyLength + 1;

        // Create new state
        FrameHistoryState newState = new FrameHistoryState(this, newIndex, newHistoryLength);

        // Process the new screen
        BytePointer newData = preProcessor.convertScreenToData(newScreen);

        // Place data in history
        frameHistory.position(newIndex).put(newData.limit(outputSize));

        // Update current frame index
        currentFrameIndex = newIndex;

        return newState;
    }

    public long frameSize() {
        return preProcessor.outputSize();
    }
}
