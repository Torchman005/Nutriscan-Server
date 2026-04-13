package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.model.Feedback;
import com.luminous.nutriscan.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Feedback> submitFeedback(@RequestBody Feedback feedback) {
        return ResponseEntity.ok(feedbackService.submitFeedback(feedback));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getUserFeedbacks(@PathVariable String userId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByUser(userId));
    }
}
