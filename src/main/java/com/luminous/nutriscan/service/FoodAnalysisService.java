package com.luminous.nutriscan.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luminous.nutriscan.model.FoodRecognitionRecord;
import com.luminous.nutriscan.repository.FoodRecognitionRecordRepository;

@Service
public class FoodAnalysisService {

    @Value("${coze.api.key}")
    private String cozeApiKey;

    @Value("${coze.workflow.id}")
    private String workflowId;

    @Value("${coze.api.base-url}")
    private String cozeApiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final MinioService minioService;
    private final TTSService ttsService;
    private final FoodRecognitionRecordRepository recordRepository;

    @Autowired
    public FoodAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper, MinioService minioService, TTSService ttsService, FoodRecognitionRecordRepository recordRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.minioService = minioService;
        this.ttsService = ttsService;
        this.recordRepository = recordRepository;
    }

    public Map<String, Object> analyzeFood(MultipartFile file) {
        return analyzeFood(file, null);
    }

    public Map<String, Object> analyzeFood(MultipartFile file, String userId) {
        try {
            // 1. 上传至MinIO
            String imageUrl = minioService.uploadFile(file);
            System.out.println("File uploaded to MinIO, URL: " + imageUrl);

            // 2. 执行工作流并保存
            return analyzeFoodByUrl(imageUrl, userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Food analysis failed: " + e.getMessage(), e);
        }
    }
    
    public Map<String, Object> analyzeFoodByUrl(String imageUrl, String userId) {
        try {
            // 1. 执行工作流
            Map<String, Object> result = runWorkflow(imageUrl);
            
            // 2. 提取文本并合成语音
            String textToRead = extractTextFromResult(result);
            if (textToRead != null && !textToRead.trim().isEmpty()) {
                String audioUrl = ttsService.synthesizeAndUpload(textToRead);
                if (audioUrl != null) {
                    result.put("audioUrl", audioUrl);
                    result.put("audioText", textToRead);
                }
            }

            // 3. 保存记录
            if (userId != null && !userId.isEmpty()) {
                saveHistory(userId, imageUrl, result);
            }
            
            return result;
        } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException("Food analysis by URL failed: " + e.getMessage(), e);
        }
    }

    private String extractTextFromResult(Map<String, Object> result) {
        if (result == null) {
            return null;
        }
        try {
            Map<?, ?> outMap = result;
            String rawContent = null;

            if (result.containsKey("output")) {
                Object output = result.get("output");
                if (output instanceof String) {
                    rawContent = (String) output;
                } else if (output instanceof Map) {
                    outMap = (Map<?, ?>) output;
                }
            } else if (result.containsKey("result") && result.size() == 1) {
                rawContent = result.get("result").toString();
            }

            if (rawContent != null && !rawContent.trim().isEmpty()) {
                String cleanText = rawContent.replaceAll("[#\\*`_]", "").trim();
                if (cleanText.length() > 200) {
                    cleanText = cleanText.substring(0, 200) + "。更多详情请查看文字内容。";
                }
                return cleanText;
            }

            StringBuilder sb = new StringBuilder("识别结果是：");
            if (outMap.containsKey("food_name")) {
                sb.append(outMap.get("food_name")).append("。");
            } else if (outMap.containsKey("name")) {
                sb.append(outMap.get("name")).append("。");
            } else {
                return "未找到具体的食物名称。";
            }
            
            if (outMap.containsKey("calories")) {
                sb.append("热量约").append(outMap.get("calories")).append("千卡。");
            }
            if (outMap.containsKey("protein") || outMap.containsKey("fat") || outMap.containsKey("carbohydrates")) {
                sb.append("其中，");
                if (outMap.containsKey("protein")) sb.append("蛋白质").append(outMap.get("protein")).append("克，");
                if (outMap.containsKey("fat")) sb.append("脂肪").append(outMap.get("fat")).append("克，");
                if (outMap.containsKey("carbohydrates")) sb.append("碳水化合物").append(outMap.get("carbohydrates")).append("克。");
            }
            String finalFallback = sb.toString().replaceAll("，。", "。");
            if (finalFallback.length() > 200) {
                finalFallback = finalFallback.substring(0, 200) + "。更多详情请查看文字内容。";
            }
            return finalFallback;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveHistory(String userId, String imageUrl, Map<String, Object> result) {
        try {
            FoodRecognitionRecord record = new FoodRecognitionRecord();
            record.setUserId(userId);
            record.setImageUrl(imageUrl);
            record.setCreatedAt(LocalDateTime.now());
            record.setRawResult(result);
            
            if (result.containsKey("output")) {
                Object output = result.get("output");
                if (output instanceof String) {
                    record.setFoodName((String) output);
                } else if (output instanceof Map) {
                     Map<?, ?> outMap = (Map<?, ?>) output;
                     if (outMap.containsKey("food_name")) {
                         record.setFoodName(outMap.get("food_name").toString());
                     }
                }
            }
            if (record.getFoodName() == null) {
                record.setFoodName("Unknown Food");
            }

            recordRepository.save(record);
        } catch (Exception e) {
            System.err.println("Failed to save history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<FoodRecognitionRecord> getHistory(String userId) {
        return recordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private String uploadFileToCoze(MultipartFile file) throws Exception {
        String url = cozeApiBaseUrl + "/files/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + cozeApiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        
        if (root.has("code") && root.get("code").asInt() == 0) {
            return root.get("data").get("id").asText();
        } else {
            throw new RuntimeException("Failed to upload file: " + response.getBody());
        }
    }

    private Map<String, Object> runWorkflow(String imageUrl) throws Exception {
        String url = cozeApiBaseUrl + "/workflow/run";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + cozeApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("input", "识别这道菜");
        parameters.put("person", "普通成年人");
        
        // Agent.py sends a String (URL or file path). 
        // We now have a MinIO URL, so we should send it as 'pic' (String).
        parameters.put("pic", imageUrl); 
        // parameters.put("image", java.util.Collections.singletonList(imageUrl)); 
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("workflow_id", workflowId);
        requestBody.put("parameters", parameters);
        
        System.out.println("Running workflow with ID: " + workflowId);
        System.out.println("Parameters: " + parameters);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        System.out.println("Coze API Response: " + response.getBody());
        JsonNode root = objectMapper.readTree(response.getBody());

        if (root.has("code") && root.get("code").asInt() == 0) {
            JsonNode dataNode = root.get("data");
            if (dataNode == null) {
                System.out.println("Warning: 'data' field is null in response.");
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("raw_response", response.getBody());
                return emptyResult;
            }
            
            String dataStr = dataNode.asText();
            try {
                return objectMapper.readValue(dataStr, Map.class);
            } catch (Exception e) {
                 // If 'data' is already an object (not stringified JSON)
                 if (dataNode.isObject()) {
                     return objectMapper.convertValue(dataNode, Map.class);
                 }
                 // If data is just a plain string (not JSON), return it wrapped
                 Map<String, Object> result = new HashMap<>();
                 result.put("result", dataStr);
                 return result;
            }
        } else {
            throw new RuntimeException("Failed to run workflow: " + response.getBody());
        }
    }

    public FoodRecognitionRecord addManualRecord(String userId, String foodName, Double calories, Double protein, Double fat, Double carbs) {
        FoodRecognitionRecord record = new FoodRecognitionRecord();
        record.setUserId(userId);
        record.setFoodName(foodName);
        record.setCalories(calories.toString());
        record.setCreatedAt(LocalDateTime.now());
        record.setImageUrl(null); // 手动录入无图片

        Map<String, Object> manualResult = new HashMap<>();
        manualResult.put("food_name", foodName);
        manualResult.put("calories", calories);
        manualResult.put("protein", protein);
        manualResult.put("fat", fat);
        manualResult.put("carbohydrates", carbs);
        manualResult.put("fiber", 0.0);
        manualResult.put("is_manual", true);

        record.setRawResult(manualResult);

        return recordRepository.save(record);
    }
}
