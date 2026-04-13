package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.model.Post;
import com.luminous.nutriscan.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.createPost(post));
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts(@RequestParam(required = false) String category) {
        if (category != null) {
            return ResponseEntity.ok(postService.getPostsByCategory(category));
        }
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<Post>> getPostsPaged(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean followedOnly,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return ResponseEntity.ok(postService.getPosts(category, search, followedOnly, userId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable String id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Post> likePost(@PathVariable String id, @RequestParam String userId) {
        return ResponseEntity.ok(postService.toggleLike(id, userId));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Post> favoritePost(@PathVariable String id, @RequestParam String userId) {
        return ResponseEntity.ok(postService.toggleFavorite(id, userId));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<Post>> getFavoritePosts(@RequestParam String userId) {
        return ResponseEntity.ok(postService.getFavoritePosts(userId));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordView(@PathVariable String id, @RequestParam String userId) {
        postService.recordView(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<Post>> getViewHistory(@RequestParam String userId) {
        return ResponseEntity.ok(postService.getViewHistory(userId));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearViewHistory(@RequestParam String userId) {
        postService.clearViewHistory(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable String userId) {
        System.out.println("Received request for user posts: " + userId);
        List<Post> posts = postService.getPostsByAuthor(userId);
        System.out.println("Found " + posts.size() + " posts for user " + userId);
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable String id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.updatePost(id, post));
    }
}
