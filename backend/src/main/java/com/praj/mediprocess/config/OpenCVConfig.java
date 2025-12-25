package com.praj.mediprocess.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OpenCVConfig {
    @PostConstruct
    public void init() {
        // Loads the native OpenCV binaries for your OS automatically
        OpenCV.loadShared();
        log.info("OpenCV native library loaded successfully.");
    }
}