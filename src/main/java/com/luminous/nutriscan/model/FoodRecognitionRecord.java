package com.luminous.nutriscan.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "food_recognition_records")
public class FoodRecognitionRecord {
    @Id
    private String id;
    private String userId;
    private String imageUrl;
    private String foodName;
    private String calories;
    private LocalDateTime createdAt;
    
    private Object rawResult;

    public FoodRecognitionRecord() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Object getRawResult() {
        return rawResult;
    }

    public void setRawResult(Object rawResult) {
        this.rawResult = rawResult;
    }
}
