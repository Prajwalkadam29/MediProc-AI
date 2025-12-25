package com.praj.mediprocess.config;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

    @Value("${tesseract.datapath:src/main/resources/tessdata}")
    private String datapath;

    @Bean
    public ITesseract tesseract() {
        ITesseract instance = new Tesseract();
        instance.setDatapath(datapath);
        instance.setLanguage("eng");
        // Use LSTM engine for better accuracy in medical records
        instance.setOcrEngineMode(1);
        return instance;
    }
}