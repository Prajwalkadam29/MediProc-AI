package com.praj.mediprocess.modules.intelligence.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel; // Using explicit implementation for stability
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpeechToTextService {

    // Using the explicit OpenAI implementation which is guaranteed to be in your classpath
    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public String transcribe(Resource audioFile) {
        // Groq Whisper is highly optimized for complex medical jargon
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);

        return response.getResult().getOutput();
    }
}
