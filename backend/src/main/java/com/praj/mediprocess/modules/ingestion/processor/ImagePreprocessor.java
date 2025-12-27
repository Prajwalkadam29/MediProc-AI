package com.praj.mediprocess.modules.ingestion.processor;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class ImagePreprocessor {

    public File preprocess(File input) {
        Mat source = Imgcodecs.imread(input.getAbsolutePath());
        Mat destination = new Mat();

        // 1. Grayscale conversion
        Imgproc.cvtColor(source, destination, Imgproc.COLOR_BGR2GRAY);

        // 2. Gaussian Blur to remove noise
        Imgproc.GaussianBlur(destination, destination, new Size(3, 3), 0);

        // 3. Adaptive Thresholding (Binarization)
        // Crucial for medical scans with shadows
        Imgproc.adaptiveThreshold(destination, destination, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

        String outputPath = input.getParent() + "/processed_" + input.getName();
        Imgcodecs.imwrite(outputPath, destination);

        return new File(outputPath);
    }
}
