package com.group2.navigation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateMessageContentRequest {

    @NotBlank(message = "content is required")
    @Size(max = 4000, message = "content must not exceed 4000 characters")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
