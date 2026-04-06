package com.group2.navigation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.navigation.dto.LoginRequest;
import com.group2.navigation.dto.SignupRequest;
import com.group2.navigation.dto.UpdateUserCredentialsRequest;
import com.group2.navigation.model.UserPreferences;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private UserRepository userRepo;
    @Autowired private MessageRepository messageRepo;

    @BeforeEach
    void cleanDb() {
        messageRepo.deleteAll();
        userRepo.deleteAll();
    }

    // ── Signup ──────────────────────────────────────────────────────────

    @Test
    void signup_validRequest_returns200() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("alice");
        req.setPassword("password123");
        req.setDisplayName("Alice");
        req.setLocation("Toronto");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.userId").isNumber())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.displayName").value("Alice"))
            .andExpect(jsonPath("$.location").value("Toronto"))
            .andExpect(jsonPath("$.preferences").exists())
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void signup_duplicateUsername_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("alice");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    @Test
    void signup_blankUsername_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void signup_shortPassword_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("bob");
        req.setPassword("short");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void signup_longPassword_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("bob");
        req.setPassword("a".repeat(101));

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void signup_invalidUsernameChars_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("alice@evil");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void signup_usernameTooShort_returns400() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("ab");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void signup_noBody_returns400() throws Exception {
        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void signup_xssInDisplayName_isSanitized() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("xssuser");
        req.setPassword("password123");
        req.setDisplayName("Test");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.displayName").value("Test"));
    }

    @Test
    void signup_nullLocation_succeeds() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setUsername("noLocation");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ── Login ───────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        createUser("alice", "password123");

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.userId").isNumber())
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        createUser("alice", "password123");

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrongpass");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_nonexistentUser_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("ghost");
        req.setPassword("password123");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_blankFields_returns400() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    // ── Get User ────────────────────────────────────────────────────────

    @Test
    void getUser_existingUser_returns200() throws Exception {
        long id = createUser("alice", "password123");

        mvc.perform(get("/api/auth/user/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getUser_nonexistentUser_returns400() throws Exception {
        mvc.perform(get("/api/auth/user/{id}", 9999))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUser_invalidId_returns400() throws Exception {
        mvc.perform(get("/api/auth/user/{id}", 0))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    void getUser_nonNumericId_returns400() throws Exception {
        mvc.perform(get("/api/auth/user/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.userId").exists());
    }

    // ── Get Preferences ─────────────────────────────────────────────────

    @Test
    void getPreferences_existingUser_returns200() throws Exception {
        long id = createUser("alice", "password123");

        mvc.perform(get("/api/auth/preferences/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.preferences").exists())
            .andExpect(jsonPath("$.preferences.wheelchairWeight").value(0.0));
    }

    @Test
    void getPreferences_nonexistentUser_returns400() throws Exception {
        mvc.perform(get("/api/auth/preferences/{id}", 9999))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ── Update Preferences ──────────────────────────────────────────────

    @Test
    void updatePreferences_validRequest_returns200() throws Exception {
        long id = createUser("alice", "password123");

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(8.0);
        prefs.setCrimeWeight(5.0);
        prefs.setLightingWeight(7.0);
        prefs.setConstructionWeight(3.0);
        prefs.setTimeOfDay(22);
        prefs.setMaxDistanceToHospital(1000.0);

        mvc.perform(put("/api/auth/preferences/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(prefs)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.preferences.wheelchairWeight").value(8.0))
            .andExpect(jsonPath("$.preferences.crimeWeight").value(5.0));
    }

    @Test
    void updatePreferences_outOfRange_returns400() throws Exception {
        long id = createUser("alice", "password123");

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(11.0);

        mvc.perform(put("/api/auth/preferences/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(prefs)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void updatePreferences_negativeWeight_returns400() throws Exception {
        long id = createUser("alice", "password123");

        UserPreferences prefs = new UserPreferences();
        prefs.setCrimeWeight(-1.0);

        mvc.perform(put("/api/auth/preferences/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(prefs)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updatePreferences_nonexistentUser_returns400() throws Exception {
        UserPreferences prefs = new UserPreferences();

        mvc.perform(put("/api/auth/preferences/{id}", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(prefs)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updatePreferences_invalidTimeOfDay_returns400() throws Exception {
        long id = createUser("alice", "password123");

        UserPreferences prefs = new UserPreferences();
        prefs.setTimeOfDay(25);

        mvc.perform(put("/api/auth/preferences/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(prefs)))
            .andExpect(status().isBadRequest());
    }

    // ── Update Credentials ──────────────────────────────────────────────

    @Test
    void updateCredentials_validEmail_returns200() throws Exception {
        long id = createUser("alice", "password123");

        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setEmail("alice@example.com");

        mvc.perform(put("/api/auth/user/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void updateCredentials_invalidEmailFormat_returns400() throws Exception {
        long id = createUser("alice", "password123");

        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setEmail("not-an-email");

        mvc.perform(put("/api/auth/user/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid email format"));
    }

    @Test
    void updateCredentials_duplicateEmail_returns400() throws Exception {
        long id1 = createUser("alice", "password123");
        long id2 = createUser("bob", "password456");

        UpdateUserCredentialsRequest req1 = new UpdateUserCredentialsRequest();
        req1.setEmail("shared@example.com");
        mvc.perform(put("/api/auth/user/{id}", id1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req1)))
            .andExpect(status().isOk());

        UpdateUserCredentialsRequest req2 = new UpdateUserCredentialsRequest();
        req2.setEmail("shared@example.com");
        mvc.perform(put("/api/auth/user/{id}", id2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req2)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Email is already in use"));
    }

    @Test
    void updateCredentials_nothingProvided_returns400() throws Exception {
        long id = createUser("alice", "password123");

        mvc.perform(put("/api/auth/user/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("At least one of email or password must be provided"));
    }

    @Test
    void updateCredentials_newPassword_canLogin() throws Exception {
        long id = createUser("alice", "password123");

        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setPassword("newpassword456");

        mvc.perform(put("/api/auth/user/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setUsername("alice");
        login.setPassword("newpassword456");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    // ── Delete User ─────────────────────────────────────────────────────

    @Test
    void deleteUser_existingUser_returns204() throws Exception {
        long id = createUser("alice", "password123");

        mvc.perform(delete("/api/auth/user/{id}", id))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/user/{id}", id))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void deleteUser_nonexistentUser_returns404() throws Exception {
        mvc.perform(delete("/api/auth/user/{id}", 9999))
            .andExpect(status().isNotFound());
    }

    // ── Get Users / Search ──────────────────────────────────────────────

    @Test
    void getUsers_noQuery_returnsAll() throws Exception {
        createUser("alice", "password123");
        createUser("bob", "password456");

        mvc.perform(get("/api/auth/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void getUsers_withQuery_filtersResults() throws Exception {
        createUser("alice", "password123");
        createUser("bob", "password456");

        mvc.perform(get("/api/auth/users").param("query", "ali"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.users[0].username").value("alice"));
    }

    @Test
    void getUsers_withExclusion_excludesUser() throws Exception {
        long aliceId = createUser("alice", "password123");
        createUser("bob", "password456");

        mvc.perform(get("/api/auth/users").param("excludeUserId", String.valueOf(aliceId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.users[0].username").value("bob"));
    }

    @Test
    void getUsers_noMatchingQuery_returnsEmpty() throws Exception {
        createUser("alice", "password123");

        mvc.perform(get("/api/auth/users").param("query", "zzz"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    // ── Helper ──────────────────────────────────────────────────────────

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
}
