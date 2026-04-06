package com.group2.navigation.service;

import com.group2.navigation.model.User;
import com.group2.navigation.model.UserPreferences;
import com.group2.navigation.repository.MessageRepository;
import com.group2.navigation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepo;
    @Autowired private MessageRepository messageRepo;

    @BeforeEach
    void cleanUp() {
        messageRepo.deleteAll();
        userRepo.deleteAll();
    }

    // --- signup ---

    @Test
    void signup_validUser_createsAccount() {
        User user = authService.signup("alice", "password123", "Alice A", "Toronto");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getDisplayName()).isEqualTo("Alice A");
        assertThat(user.getLocation()).isEqualTo("Toronto");
        // Password should be BCrypt hashed, not plaintext
        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(user.getPassword()).startsWith("$2a$");
    }

    @Test
    void signup_duplicateUsername_throws() {
        authService.signup("alice", "password123", "Alice", null);

        assertThatThrownBy(() -> authService.signup("alice", "otherpass", "Alice2", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already taken");
    }

    @Test
    void signup_nullLocation_setsNull() {
        User user = authService.signup("bob", "password123", "Bob", null);
        assertThat(user.getLocation()).isNull();
    }

    @Test
    void signup_blankLocation_setsNull() {
        User user = authService.signup("bob", "password123", "Bob", "   ");
        assertThat(user.getLocation()).isNull();
    }

    @Test
    void signup_locationTrimmed() {
        User user = authService.signup("bob", "password123", "Bob", "  Toronto  ");
        assertThat(user.getLocation()).isEqualTo("Toronto");
    }

    // --- login ---

    @Test
    void login_validCredentials_returnsUser() {
        authService.signup("alice", "password123", "Alice", null);

        User logged = authService.login("alice", "password123");
        assertThat(logged.getUsername()).isEqualTo("alice");
    }

    @Test
    void login_wrongPassword_throws() {
        authService.signup("alice", "password123", "Alice", null);

        assertThatThrownBy(() -> authService.login("alice", "wrongpass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void login_nonexistentUser_throws() {
        assertThatThrownBy(() -> authService.login("nobody", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");
    }

    // --- getUser ---

    @Test
    void getUser_exists() {
        User created = authService.signup("alice", "pass1234", "Alice", null);
        assertThat(authService.getUser(created.getId())).isPresent();
    }

    @Test
    void getUser_notExists() {
        assertThat(authService.getUser(99999L)).isEmpty();
    }

    // --- updatePreferences ---

    @Test
    void updatePreferences_savesValues() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(8.0);
        prefs.setCrimeWeight(5.0);
        prefs.setLightingWeight(3.0);
        prefs.setConstructionWeight(7.0);
        prefs.setTimeOfDay(20);
        prefs.setMaxDistanceToHospital(2000);

        User updated = authService.updatePreferences(user.getId(), prefs);

        assertThat(updated.getWheelchairWeight()).isEqualTo(8.0);
        assertThat(updated.getCrimeWeight()).isEqualTo(5.0);
        assertThat(updated.getLightingWeight()).isEqualTo(3.0);
        assertThat(updated.getConstructionWeight()).isEqualTo(7.0);
        assertThat(updated.getTimeOfDay()).isEqualTo(20);
        assertThat(updated.getMaxDistanceToHospital()).isEqualTo(2000);
    }

    @Test
    void updatePreferences_nonexistentUser_throws() {
        UserPreferences prefs = new UserPreferences();
        assertThatThrownBy(() -> authService.updatePreferences(99999L, prefs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void updatePreferences_persisted_roundTrip() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(6.0);
        authService.updatePreferences(user.getId(), prefs);

        // Fetch fresh from DB
        User fromDb = userRepo.findById(user.getId()).orElseThrow();
        UserPreferences saved = fromDb.toPreferences();
        assertThat(saved.getWheelchairWeight()).isEqualTo(6.0);
    }

    // --- updateCredentials ---

    @Test
    void updateCredentials_updateEmail() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        User updated = authService.updateCredentials(user.getId(), "alice@example.com", null);
        assertThat(updated.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void updateCredentials_updatePassword() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        authService.updateCredentials(user.getId(), null, "newpass99");

        // Should be able to login with new password
        User logged = authService.login("alice", "newpass99");
        assertThat(logged).isNotNull();
    }

    @Test
    void updateCredentials_invalidEmail_throws() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        assertThatThrownBy(() -> authService.updateCredentials(user.getId(), "not-an-email", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void updateCredentials_duplicateEmail_throws() {
        User alice = authService.signup("alice", "pass1234", "Alice", null);
        authService.updateCredentials(alice.getId(), "alice@test.com", null);

        User bob = authService.signup("bob", "pass1234", "Bob", null);
        assertThatThrownBy(() -> authService.updateCredentials(bob.getId(), "alice@test.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void updateCredentials_nothingProvided_throws() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        assertThatThrownBy(() -> authService.updateCredentials(user.getId(), null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one of email or password must be provided");
    }

    @Test
    void updateCredentials_blankEmailAndPassword_throws() {
        User user = authService.signup("alice", "pass1234", "Alice", null);

        assertThatThrownBy(() -> authService.updateCredentials(user.getId(), "  ", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one of email or password must be provided");
    }

    @Test
    void updateCredentials_nonexistentUser_throws() {
        assertThatThrownBy(() -> authService.updateCredentials(99999L, "a@b.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateCredentials_sameEmailSameUser_ok() {
        User user = authService.signup("alice", "pass1234", "Alice", null);
        authService.updateCredentials(user.getId(), "alice@test.com", null);

        // Setting same email on same user should not throw
        User updated = authService.updateCredentials(user.getId(), "alice@test.com", null);
        assertThat(updated.getEmail()).isEqualTo("alice@test.com");
    }

    // --- deleteUser ---

    @Test
    void deleteUser_removesUser() {
        User user = authService.signup("alice", "pass1234", "Alice", null);
        authService.deleteUser(user.getId());

        assertThat(userRepo.findById(user.getId())).isEmpty();
    }

    @Test
    void deleteUser_nonexistent_throws() {
        assertThatThrownBy(() -> authService.deleteUser(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteUser_cascadesMessages() {
        User alice = authService.signup("alice", "pass1234", "Alice", null);
        User bob = authService.signup("bob", "pass1234", "Bob", null);

        // Create message between them (using MessageService would be better, but direct repo works)
        com.group2.navigation.model.Message msg = com.group2.navigation.model.Message.builder()
                .sender(alice).receiver(bob).content("Hello").build();
        messageRepo.save(msg);

        authService.deleteUser(alice.getId());

        assertThat(messageRepo.findAll()).isEmpty();
        assertThat(userRepo.findById(alice.getId())).isEmpty();
        // Bob should still exist
        assertThat(userRepo.findById(bob.getId())).isPresent();
    }

    // --- searchUsers ---

    @Test
    void searchUsers_noQuery_returnsAll() {
        authService.signup("alice", "pass1234", "Alice", null);
        authService.signup("bob", "pass1234", "Bob", null);

        List<User> all = authService.searchUsers(null, null);
        assertThat(all).hasSize(2);
    }

    @Test
    void searchUsers_query_filtersResults() {
        authService.signup("alice", "pass1234", "Alice A", null);
        authService.signup("bob", "pass1234", "Bob B", null);

        List<User> results = authService.searchUsers("alice", null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    void searchUsers_excludesSelf() {
        User alice = authService.signup("alice", "pass1234", "Alice", null);
        authService.signup("bob", "pass1234", "Bob", null);

        List<User> results = authService.searchUsers(null, alice.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("bob");
    }

    @Test
    void searchUsers_blankQuery_returnsAll() {
        authService.signup("alice", "pass1234", "Alice", null);
        authService.signup("bob", "pass1234", "Bob", null);

        List<User> results = authService.searchUsers("  ", null);
        assertThat(results).hasSize(2);
    }

    @Test
    void searchUsers_matchesDisplayName() {
        authService.signup("user1", "pass1234", "Alice Anderson", null);
        authService.signup("user2", "pass1234", "Bob Builder", null);

        List<User> results = authService.searchUsers("Anderson", null);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDisplayName()).isEqualTo("Alice Anderson");
    }

    @Test
    void searchUsers_caseInsensitive() {
        authService.signup("alice", "pass1234", "Alice", null);

        List<User> results = authService.searchUsers("ALICE", null);
        assertThat(results).hasSize(1);
    }
}
