package com.luminous.nutriscan.service;

import com.luminous.nutriscan.model.Post;
import com.luminous.nutriscan.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import com.luminous.nutriscan.model.ViewHistory;
import com.luminous.nutriscan.repository.ViewHistoryRepository;
import com.luminous.nutriscan.repository.UserRepository;
import com.luminous.nutriscan.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.net.URI;
import com.luminous.nutriscan.service.MinioService;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ViewHistoryRepository viewHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MinioService minioService;

    public Post createPost(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        if (post.getStatus() == null) {
            post.setStatus("PUBLISHED");
        }
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> getPostsByCategory(String category) {
        return postRepository.findByCategory(category);
    }
    
    public Post getPost(String id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public Post toggleLike(String postId, String userId) {
        Post post = getPost(postId);
        if (post.getLikedUserIds().contains(userId)) {
            post.getLikedUserIds().remove(userId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            post.getLikedUserIds().add(userId);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        return postRepository.save(post);
    }

    public Post toggleFavorite(String postId, String userId) {
        Post post = getPost(postId);
        if (post.getFavoritedUserIds().contains(userId)) {
            post.getFavoritedUserIds().remove(userId);
            post.setFavoriteCount(Math.max(0, post.getFavoriteCount() - 1));
        } else {
            post.getFavoritedUserIds().add(userId);
            post.setFavoriteCount(post.getFavoriteCount() + 1);
        }
        return postRepository.save(post);
    }
    
    public List<Post> getFavoritePosts(String userId) {
        return postRepository.findByFavoritedUserIdsContaining(userId);
    }
    
    public void recordView(String userId, String postId) {
        ViewHistory history = viewHistoryRepository.findByUserIdAndPostId(userId, postId)
                .orElse(new ViewHistory());

        boolean isNew = history.getId() == null;

        history.setUserId(userId);
        history.setPostId(postId);
        history.setViewedAt(LocalDateTime.now());

        viewHistoryRepository.save(history);

        if (isNew) {
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                Integer viewCount = post.getViewCount();
                post.setViewCount(viewCount == null ? 1 : viewCount + 1);
                postRepository.save(post);
            }
        }
    }
    
    public List<Post> getViewHistory(String userId) {
        List<ViewHistory> historyList = viewHistoryRepository.findByUserId(userId, Sort.by(Sort.Direction.DESC, "viewedAt"));
        
        if (historyList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> postIds = historyList.stream()
                .map(ViewHistory::getPostId)
                .collect(Collectors.toList());
        
        List<Post> posts = postRepository.findAllById(postIds);
        
        // Re-sort posts based on history order (findAllById doesn't guarantee order)
        Map<String, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        
        List<Post> sortedPosts = new ArrayList<>();
        for (String postId : postIds) {
            if (postMap.containsKey(postId)) {
                sortedPosts.add(postMap.get(postId));
            }
        }
        
        return sortedPosts;
    }

    public void clearViewHistory(String userId) {
        viewHistoryRepository.deleteByUserId(userId);
    }

    public List<Post> getPostsByAuthor(String authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public Page<Post> getPosts(String category, String search, boolean followedOnly, String userId, Pageable pageable) {
        List<String> followedIds = new ArrayList<>();
        if (followedOnly && userId != null) {
            User user = userRepository.findByUserUid(userId).orElse(null);
            if (user != null && user.getFollowing() != null) {
                followedIds.addAll(user.getFollowing());
            }
        }

        if (category != null && !category.isEmpty()) {
            if (followedOnly) {
                if (search != null && !search.isEmpty()) {
                    return postRepository.searchByCategoryAndFollowed(category, followedIds, search, pageable);
                } else {
                    return postRepository.findByCategoryAndAuthorIdIn(category, followedIds, pageable);
                }
            } else {
                if (search != null && !search.isEmpty()) {
                    return postRepository.searchByCategory(category, search, pageable);
                } else {
                    return postRepository.findByCategory(category, pageable);
                }
            }
        } else {
            // Global (no category)
            if (followedOnly) {
                if (search != null && !search.isEmpty()) {
                    return postRepository.searchGlobalAndFollowed(followedIds, search, pageable);
                } else {
                    return postRepository.findByAuthorIdIn(followedIds, pageable);
                }
            } else {
                if (search != null && !search.isEmpty()) {
                    return postRepository.searchGlobal(search, pageable);
                } else {
                    return postRepository.findAll(pageable);
                }
            }
        }
    }

    public void deletePost(String id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            if (post.getImages() != null) {
                for (String imageUrl : post.getImages()) {
                    try {
                        java.net.URI uri = new java.net.URI(imageUrl);
                        String path = uri.getPath(); // /bucket/objectName
                        if (path.startsWith("/")) {
                            path = path.substring(1); // bucket/objectName
                        }
                        int firstSlashIndex = path.indexOf('/');
                        if (firstSlashIndex != -1) {
                            String objectName = path.substring(firstSlashIndex + 1);
                            minioService.deleteFile(objectName);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to delete image from Minio: " + imageUrl);
                        e.printStackTrace();
                    }
                }
            }
            postRepository.delete(post);
        }
    }

    public Post updatePost(String id, Post updatedPost) {
        Post existingPost = getPost(id);
        
        if (updatedPost.getTitle() != null) {
            existingPost.setTitle(updatedPost.getTitle());
        }
        if (updatedPost.getContent() != null) {
            existingPost.setContent(updatedPost.getContent());
        }
        if (updatedPost.getImages() != null) {
            existingPost.setImages(updatedPost.getImages());
        }
        if (updatedPost.getTags() != null) {
            existingPost.setTags(updatedPost.getTags());
        }
        if (updatedPost.getCategory() != null) {
            existingPost.setCategory(updatedPost.getCategory());
        }
        
        existingPost.setUpdatedAt(LocalDateTime.now());
        
        return postRepository.save(existingPost);
    }
}
