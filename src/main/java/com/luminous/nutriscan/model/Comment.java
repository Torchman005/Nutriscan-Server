package com.luminous.nutriscan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    private String postId;
    private String authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private int likeCount = 0;
    private java.util.List<String> likedUserIds = new java.util.ArrayList<>();
    
    private String replyToUserId;
    private String replyToUserName;
}
