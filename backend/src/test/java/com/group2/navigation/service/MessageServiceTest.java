package com.group2.navigation.service;

import com.group2.navigation.model.User;
import com.group2.navigation.repository.MessageRepository;
import com.group2.navigation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MessageServiceTest {

    @Autowired private MessageService messageService;
    @Autowired private UserRepository userRepo;
    @Autowired private MessageRepository messageRepo;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        messageRepo.deleteAll();
        userRepo.deleteAll();

        alice = new User("alice", "$2a$10$dummyhash", "Alice");
        bob = new User("bob", "$2a$10$dummyhash", "Bob");
        alice = userRepo.save(alice);
        bob = userRepo.save(bob);
    }

    // --- create ---

    @Test
    void create_validMessage_returnsMap() {
        Map<String, Object> result = messageService.create(alice.getId(), bob.getId(), "Hello Bob!");

        assertThat(result).containsKey("id");
        assertThat(result.get("senderId")).isEqualTo(alice.getId());
        assertThat(result.get("receiverId")).isEqualTo(bob.getId());
        assertThat(result.get("content")).isEqualTo("Hello Bob!");
        assertThat(result.get("timestamp")).isNotNull();
    }

    @Test
    void create_trimsContent() {
        Map<String, Object> result = messageService.create(alice.getId(), bob.getId(), "  hello  ");
        assertThat(result.get("content")).isEqualTo("hello");
    }

    @Test
    void create_selfMessage_throws() {
        assertThatThrownBy(() -> messageService.create(alice.getId(), alice.getId(), "Hi me"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sender and receiver must be different users");
    }

    @Test
    void create_nonexistentSender_throws() {
        assertThatThrownBy(() -> messageService.create(99999L, bob.getId(), "Hi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sender not found");
    }

    @Test
    void create_nonexistentReceiver_throws() {
        assertThatThrownBy(() -> messageService.create(alice.getId(), 99999L, "Hi"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Receiver not found");
    }

    // --- findInboxForReceiver ---

    @Test
    void findInbox_withMessages_returnsList() {
        messageService.create(alice.getId(), bob.getId(), "msg1");
        messageService.create(alice.getId(), bob.getId(), "msg2");

        List<Map<String, Object>> inbox = messageService.findInboxForReceiver(bob.getId());
        assertThat(inbox).hasSize(2);
    }

    @Test
    void findInbox_emptyInbox() {
        List<Map<String, Object>> inbox = messageService.findInboxForReceiver(bob.getId());
        assertThat(inbox).isEmpty();
    }

    @Test
    void findInbox_nonexistentUser_throws() {
        assertThatThrownBy(() -> messageService.findInboxForReceiver(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void findInbox_onlyShowsReceivedMessages() {
        messageService.create(alice.getId(), bob.getId(), "To Bob");
        messageService.create(bob.getId(), alice.getId(), "To Alice");

        List<Map<String, Object>> bobInbox = messageService.findInboxForReceiver(bob.getId());
        assertThat(bobInbox).hasSize(1);
        assertThat(bobInbox.get(0).get("content")).isEqualTo("To Bob");
    }

    @Test
    void findInbox_orderedByTimestampDesc() {
        messageService.create(alice.getId(), bob.getId(), "first");
        messageService.create(alice.getId(), bob.getId(), "second");

        List<Map<String, Object>> inbox = messageService.findInboxForReceiver(bob.getId());
        // Most recent first (DESC order)
        assertThat(inbox.get(0).get("content")).isEqualTo("second");
        assertThat(inbox.get(1).get("content")).isEqualTo("first");
    }

    // --- updateContent ---

    @Test
    void updateContent_changesMessage() {
        Map<String, Object> created = messageService.create(alice.getId(), bob.getId(), "original");
        Long msgId = ((Number) created.get("id")).longValue();

        Map<String, Object> updated = messageService.updateContent(msgId, "edited");
        assertThat(updated.get("content")).isEqualTo("edited");
    }

    @Test
    void updateContent_trimsContent() {
        Map<String, Object> created = messageService.create(alice.getId(), bob.getId(), "original");
        Long msgId = ((Number) created.get("id")).longValue();

        Map<String, Object> updated = messageService.updateContent(msgId, "  trimmed  ");
        assertThat(updated.get("content")).isEqualTo("trimmed");
    }

    @Test
    void updateContent_nonexistentMessage_throws() {
        assertThatThrownBy(() -> messageService.updateContent(99999L, "new content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Message not found");
    }

    // --- deleteById ---

    @Test
    void deleteById_removesMessage() {
        Map<String, Object> created = messageService.create(alice.getId(), bob.getId(), "delete me");
        Long msgId = ((Number) created.get("id")).longValue();

        messageService.deleteById(msgId);

        assertThat(messageRepo.findById(msgId)).isEmpty();
    }

    @Test
    void deleteById_multipleMessages_onlyDeletesOne() {
        messageService.create(alice.getId(), bob.getId(), "msg1");
        Map<String, Object> toDelete = messageService.create(alice.getId(), bob.getId(), "msg2");
        Long deleteId = ((Number) toDelete.get("id")).longValue();

        messageService.deleteById(deleteId);

        assertThat(messageRepo.findAll()).hasSize(1);
    }
}
