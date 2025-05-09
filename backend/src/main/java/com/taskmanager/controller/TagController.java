package com.taskmanager.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.entity.Tag;
import com.taskmanager.repository.TagRepository;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        logger.info("Recebendo requisição para listar todas as tags...");
        List<Tag> tags = tagRepository.findAll();
        logger.info("Tags encontradas: {}", tags);
        return ResponseEntity.ok(tags);
    }
}