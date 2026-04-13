package com.luminous.nutriscan.repository;

import com.luminous.nutriscan.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.Collection;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByCategory(String category);
    List<Post> findByAuthorId(String authorId);
    Page<Post> findByAuthorId(String authorId, Pageable pageable);

    List<Post> findByFavoritedUserIdsContaining(String userId);
    Page<Post> findByFavoritedUserIdsContaining(String userId, Pageable pageable);

    Page<Post> findByCategory(String category, Pageable pageable);
    
    Page<Post> findByAuthorIdIn(Collection<String> authorIds, Pageable pageable);
    
    Page<Post> findByCategoryAndAuthorIdIn(String category, Collection<String> authorIds, Pageable pageable);

    @Query("{ 'category': ?0, $or: [ { 'authorName': { $regex: ?1, $options: 'i' } }, { 'tags': { $regex: ?1, $options: 'i' } } ] }")
    Page<Post> searchByCategory(String category, String keyword, Pageable pageable);

    // Search by author or tag (global search)
    @Query("{ $or: [ { 'authorName': { $regex: ?0, $options: 'i' } }, { 'tags': { $regex: ?0, $options: 'i' } } ] }")
    Page<Post> searchGlobal(String keyword, Pageable pageable);

    @Query("{ 'category': ?0, 'authorId': { $in: ?1 }, $or: [ { 'authorName': { $regex: ?2, $options: 'i' } }, { 'tags': { $regex: ?2, $options: 'i' } } ] }")
    Page<Post> searchByCategoryAndFollowed(String category, Collection<String> authorIds, String keyword, Pageable pageable);
    
    @Query("{ 'authorId': { $in: ?0 }, $or: [ { 'authorName': { $regex: ?1, $options: 'i' } }, { 'tags': { $regex: ?1, $options: 'i' } } ] }")
    Page<Post> searchGlobalAndFollowed(Collection<String> authorIds, String keyword, Pageable pageable);
}
