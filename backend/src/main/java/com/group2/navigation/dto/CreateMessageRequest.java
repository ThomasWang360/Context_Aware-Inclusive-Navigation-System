package com.group2.navigation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {

    @NotNull(message = "senderId is required")
    @Min(value = 1, message = "senderId must be a positive number")
    private Long senderId;

    @NotNull(message = "receiverId is required")
    @Min(value = 1, message = "receiverId must be a positive number")
    private Long receiverId;

    @NotBlank(message = "content is required")
    @Size(max = 4000, message = "content must not exceed 4000 characters")
    private String content;

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
