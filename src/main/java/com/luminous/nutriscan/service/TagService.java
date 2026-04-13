package com.luminous.nutriscan.service;

import com.luminous.nutriscan.model.Tag;
import com.luminous.nutriscan.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public Tag addTag(String userId, String tagName) {
        Optional<Tag> existingTag = tagRepository.findByUserIdAndTag(userId, tagName);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }
        
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setTag(tagName);
        return tagRepository.save(tag);
    }

    public List<Tag> getTagsByUserId(String userId) {
        return tagRepository.findByUserId(userId);
    }

    public void removeTag(String userId, String tagName) {
        tagRepository.deleteByUserIdAndTag(userId, tagName);
    }
}
