package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.UserCalorieLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface UserCalorieLogRepository extends JpaRepository<UserCalorieLog, Long> {
    List<UserCalorieLog> findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(
            String userUid,
            LocalDateTime start,
            LocalDateTime end
    );
}
