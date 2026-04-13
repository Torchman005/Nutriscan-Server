package com.luminous.nutriscan.model;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "feedbacks")
public class Feedback {
    @Id
    private String id;
    private String userId;
    private String userNickname;
    private String content;
    private String contactInfo;
    private String type; // e.g., "BUG", "SUGGESTION", "OTHER"
    private LocalDateTime createdAt;
}
