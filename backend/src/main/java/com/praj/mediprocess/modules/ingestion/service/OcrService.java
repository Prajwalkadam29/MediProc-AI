package com.praj.mediprocess.modules.ingestion.service;

import com.praj.mediprocess.modules.ingestion.processor.ImagePreprocessor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final ITesseract tesseract;
    private final ImagePreprocessor preprocessor;

    public String extractText(File imageFile) throws TesseractException {
        // Preprocess first for higher accuracy
        File cleanedImage = preprocessor.preprocess(imageFile);

        // Execute OCR
        return tesseract.doOCR(cleanedImage);
    }
}
