package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.FoodRecognitionRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FoodRecognitionRecordRepository extends MongoRepository<FoodRecognitionRecord, String> {
    List<FoodRecognitionRecord> findByUserIdOrderByCreatedAtDesc(String userId);
}
