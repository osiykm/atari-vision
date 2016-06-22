package edu.brown.cs.atari_vision;

import edu.brown.cs.atari_vision.caffe.experiencereplay.FrameExperienceMemory;
import edu.brown.cs.atari_vision.caffe.experiencereplay.FrameHistoryState;
import edu.brown.cs.atari_vision.caffe.preprocess.PreProcessor;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.opencv_core.*;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maroderi on 6/16/16.
 */
public class FrameHistoryStateTest {



    @Before
    public void setup() {
        Loader.load(opencv_core.class);
    }

    @After
    public void teardown() {

    }

    @Test
    public void TestSmall() {
        BytePointer data0 = new BytePointer((byte)0, (byte)0);
        BytePointer data1 = new BytePointer((byte)0, (byte)1);
        BytePointer data2 = new BytePointer((byte)2, (byte)3);
        BytePointer data3 = new BytePointer((byte)4, (byte)5);
        BytePointer data4 = new BytePointer((byte)6, (byte)7);
        BytePointer data5 = new BytePointer((byte)8, (byte)9);
        BytePointer data6 = new BytePointer((byte)10, (byte)11);
        BytePointer data7 = new BytePointer((byte)12, (byte)13);

        Mat frame1 = new Mat(1, 2, CV_8U, data1);
        Mat frame2 = new Mat(1, 2, CV_8U, data2);
        Mat frame3 = new Mat(1, 2, CV_8U, data3);
        Mat frame4 = new Mat(1, 2, CV_8U, data4);
        Mat frame5 = new Mat(1, 2, CV_8U, data5);
        Mat frame6 = new Mat(1, 2, CV_8U, data6);
        Mat frame7 = new Mat(1, 2, CV_8U, data7);


        FrameExperienceMemory experienceMemory = new FrameExperienceMemory(5, 2, new TestPreprocessor(2));
        FrameHistoryState state0 = experienceMemory.initialState(null);
        FrameHistoryState state1 = experienceMemory.nextState(frame1, state0, null, 0, false);
        FrameHistoryState state2 = experienceMemory.nextState(frame2, state1, null, 0, false);
        FrameHistoryState state3 = experienceMemory.nextState(frame3, state2, null, 0, false);
        FrameHistoryState state4 = experienceMemory.nextState(frame4, state3, null, 0, false);

        compare(state0.getInput(), new BytePointer[]{data0, data0}, 2);
        compare(state1.getInput(), new BytePointer[]{data0, data1}, 2);
        compare(state2.getInput(), new BytePointer[]{data1, data2}, 2);
        compare(state3.getInput(), new BytePointer[]{data2, data3}, 2);
        compare(state4.getInput(), new BytePointer[]{data3, data4}, 2);

        FrameHistoryState state5 = experienceMemory.nextState(frame5, state4, null, 0, false);
        FrameHistoryState state6 = experienceMemory.nextState(frame6, state5, null, 0, false);
        FrameHistoryState state7 = experienceMemory.nextState(frame7, state6, null, 0, false);

        compare(state3.getInput(), new BytePointer[]{data2, data3}, 2);
        compare(state4.getInput(), new BytePointer[]{data3, data4}, 2);
        compare(state5.getInput(), new BytePointer[]{data4, data5}, 2);
        compare(state6.getInput(), new BytePointer[]{data5, data6}, 2);
        compare(state7.getInput(), new BytePointer[]{data6, data7}, 2);
    }

    @Test
    public void TestRandom() {
        int replaySize = 50;
        int history = 4;
        int frameSize = 10;

        FrameExperienceMemory experienceMemory = new FrameExperienceMemory(replaySize, history, new TestPreprocessor(frameSize));
        FrameHistoryState initialState = experienceMemory.initialState(null);
        BytePointer data0 = new BytePointer(history);
        for (int f = 0; f < frameSize; f++) {
            data0.position(f).put((byte)0);
        }
        data0.position(0);
        List<BytePointer> dataList = new ArrayList<>();
        for (int h = 0; h < history; h++) {
            dataList.add(data0);
        }
        compare(initialState.getInput(), dataList.toArray(new BytePointer[history]), frameSize);

        List<List<BytePointer>> dataListList = new ArrayList<>();

        List<FrameHistoryState> states = new ArrayList<>();

        FrameHistoryState prevState = initialState;

        for (int n = 0; n < 100; n++) {
            for (int i = 0; i < replaySize; i++) {
                BytePointer data = new BytePointer(frameSize);
                for (int f = 0; f < frameSize; f++) {
                    byte d = (byte) (Math.random()*126.0);
                    data.position(f).put(d);
                }
                data.position(0);
                dataList.remove(0);
                dataList.add(data);

                Mat frame = new Mat(1, frameSize, CV_8U, data);

                FrameHistoryState state = experienceMemory.nextState(frame, prevState, null, 0, false);
                prevState = state;

                compare(state.getInput(), dataList.toArray(new BytePointer[history]), frameSize);

                if (i < dataListList.size()) {
                    dataListList.set(i, new ArrayList<>(dataList));
                    states.set(i, state);
                } else {
                    dataListList.add(new ArrayList<>(dataList));
                    states.add(state);
                }

                for (int k = 0; k < states.size(); k++) {
                    compare(states.get(k).getInput(), dataListList.get(k).toArray(new BytePointer[history]), frameSize);
                }
            }
        }
    }

    public void compare(FloatPointer p, BytePointer[] dataArray, long outputSize) {
        int i = 0;
        for (BytePointer data : dataArray) {
            for (int k = 0; k < outputSize; k++) {
                Assert.assertEquals(p.get(i), data.get(k), 1e-6);
                i++;
            }
        }
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

    // DEBUG
    public static void print2D(FloatPointer ptr, int rows, int cols, int n) {

        FloatBuffer buffer = ptr.asBuffer();

        for (int i = 0; i < n; i++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    System.out.print(String.format("%.2f ", buffer.get()));
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println();
    }


    public class TestPreprocessor implements PreProcessor {
        int frameSize;

        public TestPreprocessor(int frameSize) {
            this.frameSize = frameSize;
        }

        @Override
        public BytePointer convertScreenToData(Mat screen) {
            return screen.data();
        }

        @Override
        public FloatPointer convertDataToInput(BytePointer data, long size) {
            int dataSize = outputSize() * (int)size;

            Mat mat = new Mat(1, dataSize, CV_8U, data);
            Mat floatMat = new Mat(1, dataSize, CV_32F);

            mat.convertTo(floatMat, CV_32F, 1, 0);
            return new FloatPointer(floatMat.data());
        }

        @Override
        public int outputSize() {
            return frameSize;
        }
    }
}
