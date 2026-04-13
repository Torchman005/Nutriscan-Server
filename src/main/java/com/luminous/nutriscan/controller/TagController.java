package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.model.Tag;
import com.luminous.nutriscan.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<Tag> addTag(@RequestBody Tag tag) {
        if (tag.getUserId() == null || tag.getTag() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tagService.addTag(tag.getUserId(), tag.getTag()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Tag>> getUserTags(@PathVariable String userId) {
        return ResponseEntity.ok(tagService.getTagsByUserId(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeTag(@RequestParam String userId, @RequestParam String tag) {
        tagService.removeTag(userId, tag);
        return ResponseEntity.ok().build();
    }
}
