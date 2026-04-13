package com.luminous.nutriscan.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_calorie_log")
public class UserCalorieLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, length = 64)
    private String userUid;

    @Column(nullable = false)
    private Double calories;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
