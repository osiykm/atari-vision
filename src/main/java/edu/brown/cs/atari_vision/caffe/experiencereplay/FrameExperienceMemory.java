package edu.brown.cs.atari_vision.caffe.experiencereplay;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.Action;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import edu.brown.cs.atari_vision.ale.burlap.ALEStateGenerator;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.opencv_core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bytedeco.javacpp.opencv_core.*;

/**
 * Created by maroderi on 6/20/16.
 */
public class FrameExperienceMemory implements ExperiencesMemory, ALEStateGenerator<FrameHistoryState> {

    protected BytePointer frameHistory;
    protected PreProcessor preProcessor;
    protected long currentFrameIndex;

    protected int next = 0;
    protected EnvironmentOutcome[] experiences;
    protected int size = 0;

    protected boolean alwaysIncludeMostRecent;

    int maxHistoryLength; // the history size

    public FrameExperienceMemory(int size, int maxHistoryLength, PreProcessor preProcessor) {
        this(size, maxHistoryLength, preProcessor, false);
    }

    public FrameExperienceMemory(int size, int maxHistoryLength, PreProcessor preProcessor, boolean alwaysIncludeMostRecent) {
        if(size < 1){
            throw new RuntimeException("FixedSizeMemory requires memory size > 0; was request size of " + size);
        }
        this.alwaysIncludeMostRecent = alwaysIncludeMostRecent;
        this.experiences = new EnvironmentOutcome[size];

        this.currentFrameIndex = 0;
        this.preProcessor = preProcessor;
        this.maxHistoryLength = maxHistoryLength;

        long outputSize = preProcessor.outputSize();

        // Create the frame history data size to be totalHistorySize + a padding on both sides of n - 1
        long paddingSize = (this.maxHistoryLength - 1) * outputSize;
        frameHistory = (new BytePointer(size * outputSize + 2 * paddingSize)).zero();
    }

    @Override
    public FrameHistoryState initialState(Mat screen) {
        return new FrameHistoryState(this, currentFrameIndex, 0);
    }

    @Override
    public FrameHistoryState nextState(Mat screen, FrameHistoryState prevState, Action action, double reward, boolean terminated) {
        if (prevState.index != currentFrameIndex) {
            throw new IllegalStateException("You can only update the most recent state");
        }

        long outputSize = preProcessor.outputSize();
        long frameHistoryDataSize = frameHistory.capacity();
        long paddingSize = (maxHistoryLength - 1) * outputSize;

        // Find new index
        long newIndex = prevState.index + outputSize;
        if (newIndex >= frameHistoryDataSize) {
            // Copy the buffer to the start of the history
            BytePointer frameHistoryCopy = new BytePointer(frameHistory);
            frameHistory.position(0).limit(paddingSize).put(frameHistoryCopy.position(frameHistoryDataSize - paddingSize));
            frameHistory.limit(frameHistory.capacity());

            newIndex = paddingSize;
        }

        // Increment length if smaller than n
        int newHistoryLength = prevState.historyLength >= maxHistoryLength ?
                maxHistoryLength : prevState.historyLength + 1;

        // Create new state
        FrameHistoryState newState = new FrameHistoryState(this, newIndex, newHistoryLength);

        // Process the new screen
        BytePointer newData = preProcessor.convertScreenToData(screen);

        // Place data in history
        frameHistory.position(newIndex).put(newData.limit(outputSize));

        // Update current frame index
        currentFrameIndex = newIndex;

        // Add experience
        experiences[next] = new EnvironmentOutcome(prevState, action, newState, reward, terminated);
        next = (next+1) % experiences.length;
        size = Math.min(size+1, experiences.length);

        return newState;
    }

    public FloatPointer getStateInput(FrameHistoryState state) {
        long frameSize = preProcessor.outputSize();
        long index = state.index;
        int historyLength = state.historyLength;

        // Convert compressed frame data to CNN input
        FloatPointer processedFrame = preProcessor.convertDataToInput(
                frameHistory.position(index - (historyLength - 1)*frameSize),
                historyLength);

        // Take the correct number of previous frames from the history
        FloatPointer input;
        if (historyLength < maxHistoryLength) {
            input = new FloatPointer(frameSize * maxHistoryLength);
            if (historyLength > 0) {
                input.position((maxHistoryLength - historyLength)*frameSize).put(processedFrame.limit(historyLength * frameSize));
                input.position(0).limit((maxHistoryLength - historyLength)*frameSize).zero();
                input.limit(maxHistoryLength * frameSize);
            } else {
                input.zero();
            }
        } else {
            input = processedFrame;
        }

        return input;
    }

    @Override
    public void addExperience(EnvironmentOutcome eo) {
        // Do nothing since we add the experience earlier
    }

    @Override
    public List<EnvironmentOutcome> sampleExperiences(int n) {
        List<EnvironmentOutcome> samples;

        if(this.size == 0){
            return new ArrayList<EnvironmentOutcome>();
        }

        if(this.alwaysIncludeMostRecent){
            n--;
        }

        if(this.size < n){
            samples = new ArrayList<EnvironmentOutcome>(this.size);
            for(int i = 0; i < this.size; i++){
                EnvironmentOutcome eo = this.experiences[i];
                samples.add(eo);
            }
            return samples;
        }
        else{
            samples = new ArrayList<EnvironmentOutcome>(Math.max(n, 1));
            Random r = RandomFactory.getMapped(0);
            for(int i = 0; i < n; i++) {
                int sind = r.nextInt(this.size);
                EnvironmentOutcome eo = this.experiences[sind];
                samples.add(eo);
            }
        }
        if(this.alwaysIncludeMostRecent){
            EnvironmentOutcome eo;
            if(next > 0) {
                eo = this.experiences[next - 1];
            }
            else if(size > 0){
                eo = this.experiences[this.experiences.length-1];
            }
            else{
                throw new RuntimeException("FixedSizeMemory getting most recent fails because memory is size 0.");
            }
            samples.add(eo);
        }

        return samples;
    }

    @Override
    public void resetMemory() {
        this.size = 0;
        this.next = 0;
        this.currentFrameIndex = 0;
    }
}
