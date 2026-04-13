package com.luminous.nutriscan.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_weight_log")
public class UserWeightLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, length = 64)
    private String userUid;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
