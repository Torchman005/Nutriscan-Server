package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.ViewHistory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ViewHistoryRepository extends MongoRepository<ViewHistory, String> {
    Optional<ViewHistory> findByUserIdAndPostId(String userId, String postId);
    List<ViewHistory> findByUserId(String userId, Sort sort);
    void deleteByUserId(String userId);
}
