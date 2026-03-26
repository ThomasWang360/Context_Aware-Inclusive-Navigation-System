package com.group2.navigation.service;

import com.group2.navigation.model.Message;
import com.group2.navigation.model.User;
import com.group2.navigation.repository.MessageRepository;
import com.group2.navigation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private UserRepository userRepo;

    @Transactional
    public Map<String, Object> create(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver must be different users");
        }
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content.trim())
                .build();
        Message saved = messageRepo.save(message);
        return toMap(saved);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findInboxForReceiver(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        List<Message> messages = messageRepo.findByReceiverId(userId);
        return messages.stream().map(MessageService::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> updateContent(Long messageId, String content) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        message.setContent(content.trim());
        Message saved = messageRepo.save(message);
        return toMap(saved);
    }

    @Transactional
    public void deleteById(Long messageId) {
        messageRepo.deleteById(messageId);
    }

    public static Map<String, Object> toMap(Message m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("senderId", m.getSender().getId());
        map.put("receiverId", m.getReceiver().getId());
        map.put("content", m.getContent());
        map.put("timestamp", m.getTimestamp());
        return map;
    }

}
