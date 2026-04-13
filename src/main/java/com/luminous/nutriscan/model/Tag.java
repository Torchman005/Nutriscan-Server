package com.luminous.nutriscan.model;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "tag")
public class Tag {
    @Id
    private String id;
    private String userId;
    private String tag;
}
