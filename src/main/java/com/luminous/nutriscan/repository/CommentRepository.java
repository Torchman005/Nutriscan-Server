package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostId(String postId);
    List<Comment> findByAuthorId(String authorId);
    long countByPostId(String postId);
}
