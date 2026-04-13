package com.luminous.nutriscan.service;

import com.luminous.nutriscan.dto.LoginRequest;
import com.luminous.nutriscan.model.User;
import com.luminous.nutriscan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import com.luminous.nutriscan.repository.PostRepository;
import com.luminous.nutriscan.repository.CommentRepository;
import com.luminous.nutriscan.repository.TagRepository;
import com.luminous.nutriscan.model.Post;
import com.luminous.nutriscan.model.Comment;
import com.luminous.nutriscan.model.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.luminous.nutriscan.model.UserWeightLog;
import com.luminous.nutriscan.repository.UserWeightLogRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserWeightLogRepository weightLogRepository;

    public User register(User user) {
        String phoneNumber = normalizePhone(user.getPhoneNumber());
        String wechatOpenId = normalizeOpenId(user.getWechatOpenId());
        user.setPhoneNumber(phoneNumber);
        user.setWechatOpenId(wechatOpenId);

        if (phoneNumber == null && wechatOpenId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number or WeChat openId is required");
        }

        Optional<User> existingByPhone = phoneNumber != null ? userRepository.findByPhoneNumber(phoneNumber) : Optional.empty();
        Optional<User> existingByWechat = wechatOpenId != null ? userRepository.findByWechatOpenId(wechatOpenId) : Optional.empty();

        // 修正逻辑：只要手机号查到的用户是已注销（status != 1），允许重新注册
        if (existingByPhone.isPresent() && existingByPhone.get().getStatus() != null && existingByPhone.get().getStatus() != 1) {
            // 已注销用户，允许重新注册，先删除原用户
            userRepository.delete(existingByPhone.get());
            existingByPhone = Optional.empty();
        }

        if (existingByPhone.isPresent() && existingByWechat.isPresent()) {
            if (!existingByPhone.get().getId().equals(existingByWechat.get().getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number and WeChat account belong to different users");
            }
            User existing = existingByPhone.get();
            if (phoneNumber != null && existing.getPhoneNumber() == null) {
                existing.setPhoneNumber(phoneNumber);
            }
            if (wechatOpenId != null && existing.getWechatOpenId() == null) {
                existing.setWechatOpenId(wechatOpenId);
            }
            if (user.getNickname() != null) existing.setNickname(user.getNickname());
            return userRepository.save(existing);
        } else if (existingByPhone.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already registered");
        } else if (existingByWechat.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "WeChat account already registered");
        }

        // 新用户
        if (user.getLoginTypeStr() == null) {
            if (phoneNumber != null) user.setLoginTypeStr("PHONE");
            else if (wechatOpenId != null) user.setLoginTypeStr("WECHAT");
        }

        if (user.getUserUid() == null || user.getUserUid().isEmpty()) {
            user.setUserUid(java.util.UUID.randomUUID().toString());
        }

        User savedUser = userRepository.save(user);
        fillUserTags(savedUser);
        recordWeightIfPresent(savedUser.getUserUid(), savedUser.getWeight());
        return savedUser;
    }

    public User login(LoginRequest request) {
        User user;
        if ("PHONE".equalsIgnoreCase(request.getLoginType())) {
            String phoneNumber = normalizePhone(request.getPhoneNumber());
            if (phoneNumber == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
            }
            if (!"123456".equals(request.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid verification code");
            }
            user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else if ("WECHAT".equalsIgnoreCase(request.getLoginType())) {
            String wechatOpenId = normalizeOpenId(request.getWechatOpenId());
            if (wechatOpenId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WeChat openId is required");
            }
            user = userRepository.findByWechatOpenId(wechatOpenId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid login type");
        }
        
        fillUserTags(user);
        return user;
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeOpenId(String openId) {
        if (openId == null) {
            return null;
        }
        String trimmed = openId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional
    public User updateUser(String uid, User updatedUser) {
        // ... (existing update logic)
        User existingUser = userRepository.findByUserUid(uid).orElse(null);
        if (existingUser == null) {
            try {
                Long numericId = Long.valueOf(uid);
                existingUser = userRepository.findById(numericId).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        if (updatedUser.getNickname() != null) existingUser.setNickname(updatedUser.getNickname());
        if (updatedUser.getGender() != null) existingUser.setGender(updatedUser.getGender());
        if (updatedUser.getBirthDate() != null) existingUser.setBirthDate(updatedUser.getBirthDate());
        if (updatedUser.getHeight() != null) existingUser.setHeight(updatedUser.getHeight());
        if (updatedUser.getWeight() != null) existingUser.setWeight(updatedUser.getWeight());
        if (updatedUser.getGroupCategory() != null) existingUser.setGroupCategory(updatedUser.getGroupCategory());
        if (updatedUser.getAvatarUrl() != null) existingUser.setAvatarUrl(updatedUser.getAvatarUrl());
        if (updatedUser.getRegion() != null) existingUser.setRegion(updatedUser.getRegion());
        if (updatedUser.getBio() != null) existingUser.setBio(updatedUser.getBio());

        if (updatedUser.getFollowing() != null) {
            existingUser.setFollowing(updatedUser.getFollowing());
        }
        
        if (updatedUser.getTargetWeight() != null) existingUser.setTargetWeight(updatedUser.getTargetWeight());
        if (updatedUser.getWaistline() != null) existingUser.setWaistline(updatedUser.getWaistline());
        
        User savedUser = userRepository.save(existingUser);

        if (updatedUser.getWeight() != null) {
            recordWeightIfPresent(savedUser.getUserUid(), savedUser.getWeight());
        }

        // 同步更新关联数据
        if (updatedUser.getNickname() != null || updatedUser.getAvatarUrl() != null) {
            // ... (sync logic)
            String newNickname = savedUser.getNickname();
            String newAvatar = savedUser.getAvatarUrl();
            String userId = savedUser.getUserUid(); // Use userUid as the authorId in posts/comments 
             System.out.println("Syncing profile updates for user: " + userId);
             
             List<Post> posts = postRepository.findByAuthorId(userId);
             System.out.println("Found " + posts.size() + " posts for user " + userId);
             
             for (Post post : posts) {
                 boolean changed = false;
                 if (updatedUser.getNickname() != null) {
                     post.setAuthorName(newNickname);
                     changed = true;
                 }
                 if (newAvatar != null) {
                     post.setAuthorAvatar(newAvatar);
                     changed = true;
                 }
                 if (changed) {
                     postRepository.save(post);
                     System.out.println("Updated post: " + post.getId());
                 }
             }
             
             List<Comment> comments = commentRepository.findByAuthorId(userId);
             System.out.println("Found " + comments.size() + " comments for user " + userId);
             
             for (Comment comment : comments) {
                 boolean changed = false;
                 if (updatedUser.getNickname() != null) {
                     comment.setAuthorName(newNickname);
                     changed = true;
                 }
                 if (newAvatar != null) {
                     comment.setAuthorAvatar(newAvatar);
                     changed = true;
                 }
                 if (changed) {
                     commentRepository.save(comment);
                     System.out.println("Updated comment: " + comment.getId());
                 }
             }
        }
        
        fillUserTags(savedUser);
        return savedUser;
    }
    
    public User getUser(String uid) {
        User user = userRepository.findByUserUid(uid).orElse(null);
        if (user == null) {
            try {
                Long numericId = Long.valueOf(uid);
                user = userRepository.findById(numericId).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        fillUserTags(user);
        return user;
    }

    private void fillUserTags(User user) {
        if (user != null && user.getUserUid() != null) {
            List<Tag> tags = tagRepository.findByUserId(user.getUserUid());
            user.setCustomTags(tags.stream().map(Tag::getTag).collect(Collectors.toList()));
        }
    }

    private void recordWeightIfPresent(String userUid, Double weight) {
        if (userUid == null || weight == null) {
            return;
        }
        java.time.LocalDateTime now = LocalDateTime.now();
        java.time.LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        java.time.LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

        // 查询当天是否已有记录
        java.util.Optional<UserWeightLog> existingLogOpt = weightLogRepository
                .findTopByUserUidAndRecordedAtBetweenOrderByRecordedAtDesc(userUid, startOfDay, endOfDay);
        UserWeightLog log;
        if (existingLogOpt.isPresent()) {
            // 覆盖当天最后一次记录
            log = existingLogOpt.get();
            log.setWeight(weight);
            log.setRecordedAt(now); // 更新时间
        } else {
            // 新建一条记录
            log = new UserWeightLog();
            log.setUserUid(userUid);
            log.setWeight(weight);
            log.setRecordedAt(now);
        }
        weightLogRepository.save(log);
    }

    @Transactional
    public User updateWeight(String uid, Double weight) {
        if (weight == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weight is required");
        }
        User existingUser = userRepository.findByUserUid(uid).orElse(null);
        if (existingUser == null) {
            try {
                Long numericId = Long.valueOf(uid);
                existingUser = userRepository.findById(numericId).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }
        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        existingUser.setWeight(weight);
        User savedUser = userRepository.save(existingUser);
        // 体重写入 user_weight_log
        recordWeightIfPresent(savedUser.getUserUid(), savedUser.getWeight());
        fillUserTags(savedUser);
        return savedUser;
    }
}
