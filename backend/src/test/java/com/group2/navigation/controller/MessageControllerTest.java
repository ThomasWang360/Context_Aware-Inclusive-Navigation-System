package com.group2.navigation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.navigation.dto.CreateMessageRequest;
import com.group2.navigation.dto.SignupRequest;
import com.group2.navigation.dto.UpdateMessageContentRequest;
import com.group2.navigation.repository.MessageRepository;
import com.group2.navigation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository userRepo;
    @Autowired private MessageRepository messageRepo;

    private long aliceId;
    private long bobId;

    @BeforeEach
    void setUp() throws Exception {
        messageRepo.deleteAll();
        userRepo.deleteAll();
        aliceId = createUser("alice", "password123");
        bobId = createUser("bob", "password456");
    }

    // ── Create Message ──────────────────────────────────────────────────

    @Test
    void createMessage_validRequest_returns201() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(aliceId);
        req.setReceiverId(bobId);
        req.setContent("Hello Bob!");

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.senderId").value(aliceId))
            .andExpect(jsonPath("$.receiverId").value(bobId))
            .andExpect(jsonPath("$.content").value("Hello Bob!"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createMessage_selfMessage_returns400() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(aliceId);
        req.setReceiverId(aliceId);
        req.setContent("Talking to myself");

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Sender and receiver must be different users"));
    }

    @Test
    void createMessage_blankContent_returns400() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(aliceId);
        req.setReceiverId(bobId);
        req.setContent("");

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.content").exists());
    }

    @Test
    void createMessage_nullSenderId_returns400() throws Exception {
        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"receiverId\": 1, \"content\": \"Hello\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.senderId").exists());
    }

    @Test
    void createMessage_nonexistentSender_returns400() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(9999L);
        req.setReceiverId(bobId);
        req.setContent("Ghost message");

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Sender not found"));
    }

    @Test
    void createMessage_nonexistentReceiver_returns400() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(aliceId);
        req.setReceiverId(9999L);
        req.setContent("Hello nobody");

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Receiver not found"));
    }

    @Test
    void createMessage_contentTrimmed() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(aliceId);
        req.setReceiverId(bobId);
        req.setContent("  padded  ");

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("padded"));
    }

    @Test
    void createMessage_contentTooLong_returns400() throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(aliceId);
        req.setReceiverId(bobId);
        req.setContent("x".repeat(4001));

        mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── Get Inbox ───────────────────────────────────────────────────────

    @Test
    void getInbox_withMessages_returnsAll() throws Exception {
        sendMessage(aliceId, bobId, "Hello!");
        sendMessage(aliceId, bobId, "How are you?");

        mvc.perform(get("/api/messages/{userId}", bobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.messages").isArray());
    }

    @Test
    void getInbox_emptyInbox_returnsEmptyList() throws Exception {
        mvc.perform(get("/api/messages/{userId}", aliceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.messages").isEmpty());
    }

    @Test
    void getInbox_nonexistentUser_returns400() throws Exception {
        mvc.perform(get("/api/messages/{userId}", 9999))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getInbox_invalidId_returns400() throws Exception {
        mvc.perform(get("/api/messages/abc"))
            .andExpect(status().isBadRequest());
    }

    // ── Update Message ──────────────────────────────────────────────────

    @Test
    void updateMessage_validRequest_returns200() throws Exception {
        long msgId = sendMessage(aliceId, bobId, "Original");

        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("Updated");

        mvc.perform(put("/api/messages/{id}", msgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void updateMessage_nonexistentMessage_returns400() throws Exception {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("Update ghost");

        mvc.perform(put("/api/messages/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Message not found"));
    }

    @Test
    void updateMessage_blankContent_returns400() throws Exception {
        long msgId = sendMessage(aliceId, bobId, "Original");

        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("");

        mvc.perform(put("/api/messages/{id}", msgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ── Delete Message ──────────────────────────────────────────────────

    @Test
    void deleteMessage_existingMessage_returns204() throws Exception {
        long msgId = sendMessage(aliceId, bobId, "To be deleted");

        mvc.perform(delete("/api/messages/{id}", msgId))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteMessage_afterDeletion_messageGone() throws Exception {
        long msgId = sendMessage(aliceId, bobId, "Ephemeral");

        mvc.perform(delete("/api/messages/{id}", msgId))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/messages/{userId}", bobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    // ── Cascading Delete ────────────────────────────────────────────────

    @Test
    void deleteUser_deletesRelatedMessages() throws Exception {
        sendMessage(aliceId, bobId, "Should be deleted");
        sendMessage(bobId, aliceId, "Also deleted");

        mvc.perform(delete("/api/auth/user/{id}", aliceId))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/messages/{userId}", bobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private long createUser(String username, String password) throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername(username);
        req.setPassword(password);

        MvcResult result = mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .get("userId").asLong();
    }

    private long sendMessage(long senderId, long receiverId, String content) throws Exception {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(senderId);
        req.setReceiverId(receiverId);
        req.setContent(content);

        MvcResult result = mvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();

        return mapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
