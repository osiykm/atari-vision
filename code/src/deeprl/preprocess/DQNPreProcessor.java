package deeprl.preprocess;

import ale.burlap.ALEState;
import ale.screen.ColorPalette;
import ale.screen.NTSCPalette;
import ale.screen.ScreenMatrix;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.awt.*;

/**
 * Created by MelRod on 5/25/16.
 */
public class DQNPreProcessor implements PreProcessor {

    /** The map from screen indices to RGB colors */
    protected ColorPalette colorMap;

    public DQNPreProcessor() {
        this.colorMap = new NTSCPalette();
    }

    @Override
    public INDArray convertScreenToInput(ScreenMatrix screen) {

        int rows = 110;
        int cols = 84;

        INDArray downsample = downsample(screen, rows, cols);

        int rowCropStart = 20;
        int rowCropEnd = rowCropStart + 84;
        INDArray crop = crop(downsample, NDArrayIndex.interval(rowCropStart, rowCropEnd), NDArrayIndex.all());
        INDArray linear = crop.reshape(1, 84*84);
        return linear;
    }

    @Override
    public int inputSize() {
        return 84*84;
    }

    protected INDArray downsample(ScreenMatrix screen, int rows, int cols) {
        double rowRatio = screen.height/(double)rows;
        double colRatio = screen.width/(double)cols;

        INDArray downSample = Nd4j.zeros(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                int cIndex = screen.matrix[(int)(c * colRatio)][(int)(r * rowRatio)];
                Color color = colorMap.get(cIndex);

                double gray = (color.getRed() + color.getGreen() + color.getBlue())/3.0;
                downSample.putScalar(new int[]{r, c}, gray);
            }
        }

        return downSample;
    }

    public INDArray crop(INDArray input, INDArrayIndex rows, INDArrayIndex cols) {
        return input.get(rows, cols);
    }
}
