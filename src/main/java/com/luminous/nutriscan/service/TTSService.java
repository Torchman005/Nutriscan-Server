package com.luminous.nutriscan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class TTSService {

    @Value("${volcengine.tts.app-id}")
    private String appId;

    @Value("${volcengine.tts.token}")
    private String token;

    @Value("${volcengine.tts.cluster}")
    private String cluster;

    @Value("${volcengine.tts.voice-type}")
    private String voiceType;

    @Value("${volcengine.tts.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final MinioService minioService;

    @Autowired
    public TTSService(RestTemplate restTemplate, ObjectMapper objectMapper, MinioService minioService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.minioService = minioService;
    }

    /**
     * Synthesizes text to speech and uploads the resulting audio to MinIO.
     * @param text The text to read
     * @return The URL of the uploaded audio file
     */
    public String synthesizeAndUpload(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer; " + token);
            headers.set("Content-Type", "application/json");

            Map<String, Object> payload = new HashMap<>();
            
            Map<String, Object> app = new HashMap<>();
            app.put("appid", appId);
            app.put("token", token);
            app.put("cluster", cluster);
            payload.put("app", app);

            Map<String, Object> user = new HashMap<>();
            user.put("uid", "nutriscan_user_" + UUID.randomUUID().toString().substring(0, 8));
            payload.put("user", user);

            Map<String, Object> audio = new HashMap<>();
            audio.put("voice_type", voiceType);
            audio.put("encoding", "mp3");
            audio.put("speed_ratio", 1.0);
            audio.put("volume_ratio", 1.0);
            audio.put("pitch_ratio", 1.0);
            payload.put("audio", audio);

            Map<String, Object> request = new HashMap<>();
            request.put("reqid", UUID.randomUUID().toString());
            request.put("text", text);
            request.put("text_type", "plain");
            request.put("operation", "query");
            payload.put("request", request);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("code") && root.get("code").asInt() == 3000) {
                String base64Audio = root.get("data").asText();
                byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
                
                // Upload to MinIO
                String fileName = "tts_" + UUID.randomUUID().toString() + ".mp3";
                InputStream inputStream = new ByteArrayInputStream(audioBytes);
                
                return minioService.uploadFile(inputStream, audioBytes.length, fileName, "audio/mpeg");
            } else {
                throw new RuntimeException("TTS synthesis failed: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("TTS Error: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null if failed, so it doesn't break the main flow
        }
    }
}