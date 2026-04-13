package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.UserWeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserWeightLogRepository extends JpaRepository<UserWeightLog, Long> {
    List<UserWeightLog> findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(
            String userUid,
            LocalDateTime start,
            LocalDateTime end
    );

    java.util.Optional<UserWeightLog> findTopByUserUidAndRecordedAtBetweenOrderByRecordedAtDesc(
            String userUid,
            LocalDateTime start,
            LocalDateTime end
    );
}
