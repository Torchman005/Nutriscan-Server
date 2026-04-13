package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.dto.LoginRequest;
import com.luminous.nutriscan.dto.WeightUpdateRequest;
import com.luminous.nutriscan.model.User;
import com.luminous.nutriscan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
    
    @PutMapping("/{id}/weight")
    public ResponseEntity<User> updateWeight(@PathVariable String id, @RequestBody WeightUpdateRequest request) {
        return ResponseEntity.ok(userService.updateWeight(id, request.getWeight()));
    }

    @PostMapping("/{id}/follow/{targetId}")
    public ResponseEntity<User> followUser(@PathVariable String id, @PathVariable String targetId) {
        User user = userService.getUser(id);
        if (user.getFollowing() == null) {
            user.setFollowing(new java.util.HashSet<>());
        }
        user.getFollowing().add(targetId);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PostMapping("/{id}/unfollow/{targetId}")
    public ResponseEntity<User> unfollowUser(@PathVariable String id, @PathVariable String targetId) {
        User user = userService.getUser(id);
        if (user.getFollowing() != null) {
            user.getFollowing().remove(targetId);
        }
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
}
