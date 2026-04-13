package com.luminous.nutriscan.service;

import com.luminous.nutriscan.model.UserCalorieLog;
import com.luminous.nutriscan.repository.UserCalorieLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCalorieLogService {

    @Autowired
    private UserCalorieLogRepository repository;

    public UserCalorieLog addRecord(String userUid, Double calories, LocalDateTime recordedAt) {
        UserCalorieLog log = new UserCalorieLog();
        log.setUserUid(userUid);
        log.setCalories(calories);
        log.setRecordedAt(recordedAt != null ? recordedAt : LocalDateTime.now());
        return repository.save(log);
    }

    public List<UserCalorieLog> getRecords(String userUid, LocalDateTime start, LocalDateTime end) {
        return repository.findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(userUid, start, end);
    }
}
