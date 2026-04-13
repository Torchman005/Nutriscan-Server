package com.luminous.nutriscan.service;

import com.luminous.nutriscan.model.Comment;
import com.luminous.nutriscan.model.Post;
import com.luminous.nutriscan.repository.CommentRepository;
import com.luminous.nutriscan.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    public Comment addComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        if (comment.getLikedUserIds() == null) {
            comment.setLikedUserIds(new java.util.ArrayList<>());
        }
        Comment saved = commentRepository.save(comment);
        updatePostCommentCount(saved.getPostId());
        return saved;
    }

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment toggleLike(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null) {
            if (comment.getLikedUserIds() == null) {
                comment.setLikedUserIds(new java.util.ArrayList<>());
            }
            
            if (comment.getLikedUserIds().contains(userId)) {
                comment.getLikedUserIds().remove(userId);
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            } else {
                comment.getLikedUserIds().add(userId);
                comment.setLikeCount(comment.getLikeCount() + 1);
            }
            return commentRepository.save(comment);
        }
        return null;
    }

    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        String postId = comment != null ? comment.getPostId() : null;

        commentRepository.deleteById(commentId);
        
        List<Comment> allComments = commentRepository.findAll();
        deleteRepliesRecursively(commentId, allComments);

        if (postId != null) {
            updatePostCommentCount(postId);
        }
    }

    private void updatePostCommentCount(String postId) {
        if (postId == null) {
            return;
        }
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return;
        }
        long count = commentRepository.countByPostId(postId);
        post.setCommentCount((int) count);
        postRepository.save(post);
    }

    private void deleteRepliesRecursively(String parentId, List<Comment> allComments) {
        List<Comment> directReplies = allComments.stream()
                .filter(c -> parentId.equals(c.getReplyToUserId()))
                .collect(java.util.stream.Collectors.toList());
        
        for (Comment reply : directReplies) {
            deleteRepliesRecursively(reply.getId(), allComments);
            commentRepository.deleteById(reply.getId());
        }
    }
}
