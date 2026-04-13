package com.luminous.nutriscan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Data
@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    private String authorId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String content;
    private List<String> images;
    private List<String> tags;
    private String category; // "WELLNESS", "FITNESS", "TODDLER"
    private String status; // "DRAFT", "PUBLISHED", "DELETED"
    private Integer likeCount = 0;
    private Integer favoriteCount = 0;
    private Integer commentCount = 0;
    private Integer viewCount = 0;

    private Set<String> likedUserIds = new HashSet<>();
    private Set<String> favoritedUserIds = new HashSet<>();
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
