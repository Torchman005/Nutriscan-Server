package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.service.FoodAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import com.luminous.nutriscan.model.FoodRecognitionRecord;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class FoodAnalysisController {

    private final FoodAnalysisService foodAnalysisService;

    public FoodAnalysisController(FoodAnalysisService foodAnalysisService) {
        this.foodAnalysisService = foodAnalysisService;
    }

    @PostMapping("/food")
    public ResponseEntity<Map<String, Object>> analyzeFood(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId) {
        try {
            Map<String, Object> result = foodAnalysisService.analyzeFood(file, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/food/url")
    public ResponseEntity<Map<String, Object>> analyzeFoodByUrl(
            @RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            String userId = request.get("userId");
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "imageUrl is required"));
            }
            
            Map<String, Object> result = foodAnalysisService.analyzeFoodByUrl(imageUrl, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<FoodRecognitionRecord>> getHistory(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(foodAnalysisService.getHistory(userId));
    }

    @PostMapping("/manual")
    public ResponseEntity<FoodRecognitionRecord> addManualRecord(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String foodName = (String) request.get("foodName");
        Object caloriesObj = request.get("calories");
        Double calories = 0.0;
        if (caloriesObj instanceof Number) {
            calories = ((Number) caloriesObj).doubleValue();
        } else if (caloriesObj instanceof String) {
            calories = Double.parseDouble((String) caloriesObj);
        }

        Double protein = request.containsKey("protein") ? Double.parseDouble(request.get("protein").toString()) : 0.0;
        Double fat = request.containsKey("fat") ? Double.parseDouble(request.get("fat").toString()) : 0.0;
        Double carbs = request.containsKey("carbohydrates") ? Double.parseDouble(request.get("carbohydrates").toString()) : 0.0;

        FoodRecognitionRecord record = foodAnalysisService.addManualRecord(userId, foodName, calories, protein, fat, carbs);
        return ResponseEntity.ok(record);
    }
}
