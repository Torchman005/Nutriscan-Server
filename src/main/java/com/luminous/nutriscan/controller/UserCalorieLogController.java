package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.model.UserCalorieLog;
import com.luminous.nutriscan.service.UserCalorieLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calories")
public class UserCalorieLogController {

    @Autowired
    private UserCalorieLogService service;

    @PostMapping
    public ResponseEntity<UserCalorieLog> addRecord(@RequestBody CalorieRecordRequest request) {
        if (request.userId == null || request.userId.isBlank() || request.calories == null) {
            return ResponseEntity.badRequest().build();
        }
        UserCalorieLog saved = service.addRecord(request.userId, request.calories, request.recordedAt);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<UserCalorieLog>> getRecords(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return ResponseEntity.ok(service.getRecords(userId, start, end));
    }

    public static class CalorieRecordRequest {
        public String userId;
        public Double calories;
        public LocalDateTime recordedAt;
    }
}
