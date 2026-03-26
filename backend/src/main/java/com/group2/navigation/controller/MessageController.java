package com.group2.navigation.controller;

import com.group2.navigation.dto.CreateMessageRequest;
import com.group2.navigation.dto.UpdateMessageContentRequest;
import com.group2.navigation.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
@Validated
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody CreateMessageRequest body) {
        try {
            Map<String, Object> created = messageService.create(
                    body.getSenderId(), body.getReceiverId(), body.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> inbox(
            @PathVariable @Min(value = 1, message = "userId must be a positive number") Long userId) {
        try {
            List<Map<String, Object>> messages = messageService.findInboxForReceiver(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messages", messages,
                    "count", messages.size()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable @Min(value = 1, message = "id must be a positive number") Long id,
            @Valid @RequestBody UpdateMessageContentRequest body) {
        try {
            Map<String, Object> updated = messageService.updateContent(id, body.getContent());
            return ResponseEntity.ok(new LinkedHashMap<>(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Min(value = 1, message = "id must be a positive number") Long id) {
        messageService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
