package com.luminous.nutriscan.service;

import com.luminous.nutriscan.model.Feedback;
import com.luminous.nutriscan.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback submitFeedback(Feedback feedback) {
        feedback.setCreatedAt(LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedbacksByUser(String userId) {
        return feedbackRepository.findByUserId(userId);
    }
}
