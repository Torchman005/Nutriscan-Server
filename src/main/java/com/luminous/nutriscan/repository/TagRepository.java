package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends MongoRepository<Tag, String> {
    List<Tag> findByUserId(String userId);
    Optional<Tag> findByUserIdAndTag(String userId, String tag);
    void deleteByUserIdAndTag(String userId, String tag);
}
