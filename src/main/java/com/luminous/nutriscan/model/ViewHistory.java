package com.luminous.nutriscan.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "view_history")
public class ViewHistory {
    @Id
    private String id;
    private String userId;
    private String postId;
    private LocalDateTime viewedAt;
}
