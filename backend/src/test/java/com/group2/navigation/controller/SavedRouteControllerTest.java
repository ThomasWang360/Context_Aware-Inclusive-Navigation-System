package com.group2.navigation.controller;

import com.group2.navigation.model.User;
import com.group2.navigation.repository.SavedRouteRepository;
import com.group2.navigation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SavedRouteControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository userRepo;
    @Autowired private SavedRouteRepository routeRepo;

    private Long userId;

    @BeforeEach
    void setUp() {
        routeRepo.deleteAll();
        userRepo.deleteAll();
        User user = new User("routeuser", "hashed", "Route User");
        userId = userRepo.save(user).getId();
    }

    private String routeJson(Long uid, String name) {
        return """
            {
              "userId": %d,
              "name": "%s",
              "startAddress": "123 Queen St W",
              "endAddress": "456 King St E",
              "startLat": 43.65,
              "startLng": -79.38,
              "endLat": 43.66,
              "endLng": -79.37,
              "wheelchairWeight": 5.0,
              "crimeWeight": 3.0,
              "lightingWeight": 7.0,
              "constructionWeight": 2.0,
              "timeOfDay": 14,
              "maxDistanceToHospital": 500.0
            }
            """.formatted(uid, name);
    }

    // ---- CREATE ----

    @Test
    void create_validBody_returns201() throws Exception {
        mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "My Route")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("My Route"))
            .andExpect(jsonPath("$.userId").value(userId))
            .andExpect(jsonPath("$.startAddress").value("123 Queen St W"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        String json = """
            { "userId": %d, "startLat": 43.65, "startLng": -79.38, "endLat": 43.66, "endLng": -79.37 }
            """.formatted(userId);
        mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void create_badUserId_returns400() throws Exception {
        mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(99999L, "No User")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_exceedsMaxRoutes_returns400() throws Exception {
        for (int i = 0; i < 20; i++) {
            mvc.perform(post("/api/saved-routes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(routeJson(userId, "Route " + i)))
                .andExpect(status().isCreated());
        }
        // 21st should fail
        mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Route 21")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Maximum")));
    }

    // ---- GET ----

    @Test
    void getByUser_returnsRoutes() throws Exception {
        mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "First")))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/saved-routes/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routes", hasSize(1)))
            .andExpect(jsonPath("$.routes[0].name").value("First"));
    }

    @Test
    void getByUser_noRoutes_returnsEmptyList() throws Exception {
        mvc.perform(get("/api/saved-routes/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routes", hasSize(0)));
    }

    @Test
    void getByUser_unknownUser_returns400() throws Exception {
        mvc.perform(get("/api/saved-routes/{userId}", 99999L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    // ---- UPDATE ----

    @Test
    void update_validBody_returnsUpdated() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Original")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        // Extract the id
        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        mvc.perform(put("/api/saved-routes/{routeId}", routeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Updated")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void update_unknownRoute_returns400() throws Exception {
        mvc.perform(put("/api/saved-routes/{routeId}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Nope")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void update_wrongUser_returns400() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Mine")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        User otherUser = new User("other", "hashed", "Other");
        Long otherId = userRepo.save(otherUser).getId();

        mvc.perform(put("/api/saved-routes/{routeId}", routeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(otherId, "Stolen")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("does not belong")));
    }

    // ---- DELETE ----

    @Test
    void delete_validOwner_returns200() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "To Delete")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        mvc.perform(delete("/api/saved-routes/{routeId}", routeId)
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Verify it's gone
        mvc.perform(get("/api/saved-routes/{userId}", userId))
            .andExpect(jsonPath("$.routes", hasSize(0)));
    }

    @Test
    void delete_wrongUser_returns400() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Protected")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        User otherUser = new User("other2", "hashed", "Other2");
        Long otherId = userRepo.save(otherUser).getId();

        mvc.perform(delete("/api/saved-routes/{routeId}", routeId)
                .param("userId", otherId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("does not belong")));
    }

    @Test
    void delete_unknownRoute_returns400() throws Exception {
        mvc.perform(delete("/api/saved-routes/{routeId}", 99999L)
                .param("userId", userId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    // ---- CASCADE: deleting user removes saved routes ----

    @Test
    void cascadeDelete_userDeletion_removesRoutes() throws Exception {
        mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Cascade Test")))
            .andExpect(status().isCreated());

        org.assertj.core.api.Assertions.assertThat(routeRepo.countByUserId(userId)).isEqualTo(1);

        // Delete user via API
        mvc.perform(delete("/api/auth/user/{id}", userId))
            .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(routeRepo.countByUserId(userId)).isEqualTo(0);
    }

    // ---- SHARE ----

    @Test
    void share_validRequest_returns200() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "Shareable")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        User target = new User("sharetarget", "hashed", "Target");
        Long targetId = userRepo.save(target).getId();

        mvc.perform(post("/api/saved-routes/{routeId}/share", routeId)
                .param("userId", userId.toString())
                .param("targetUserId", targetId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.route.sharedWith", hasSize(1)));
    }

    @Test
    void share_withSelf_returns400() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "SelfShare")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        mvc.perform(post("/api/saved-routes/{routeId}/share", routeId)
                .param("userId", userId.toString())
                .param("targetUserId", userId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("yourself")));
    }

    @Test
    void share_wrongOwner_returns400() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "NotYours")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        User other = new User("shareother", "hashed", "Other");
        Long otherId = userRepo.save(other).getId();

        User target = new User("sharetarget2", "hashed", "Target2");
        Long targetId = userRepo.save(target).getId();

        mvc.perform(post("/api/saved-routes/{routeId}/share", routeId)
                .param("userId", otherId.toString())
                .param("targetUserId", targetId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("does not belong")));
    }

    // ---- UNSHARE ----

    @Test
    void unshare_validRequest_returns200() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "ToUnshare")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        User target = new User("unsharetgt", "hashed", "Target");
        Long targetId = userRepo.save(target).getId();

        // Share first
        mvc.perform(post("/api/saved-routes/{routeId}/share", routeId)
                .param("userId", userId.toString())
                .param("targetUserId", targetId.toString()))
            .andExpect(status().isOk());

        // Then unshare
        mvc.perform(delete("/api/saved-routes/{routeId}/share", routeId)
                .param("userId", userId.toString())
                .param("targetUserId", targetId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.route.sharedWith", hasSize(0)));
    }

    // ---- GET SHARED ----

    @Test
    void getSharedWithUser_returnsSharedRoutes() throws Exception {
        String createResult = mvc.perform(post("/api/saved-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(routeJson(userId, "SharedRoute")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long routeId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResult).get("id").asLong();

        User target = new User("getshared", "hashed", "Target");
        Long targetId = userRepo.save(target).getId();

        // Share
        mvc.perform(post("/api/saved-routes/{routeId}/share", routeId)
                .param("userId", userId.toString())
                .param("targetUserId", targetId.toString()))
            .andExpect(status().isOk());

        // Get shared routes for the target
        mvc.perform(get("/api/saved-routes/shared/{userId}", targetId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routes", hasSize(1)))
            .andExpect(jsonPath("$.routes[0].name").value("SharedRoute"));
    }

    @Test
    void getSharedWithUser_noShares_returnsEmpty() throws Exception {
        mvc.perform(get("/api/saved-routes/shared/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.routes", hasSize(0)));
    }

    @Test
    void getSharedWithUser_unknownUser_returns400() throws Exception {
        mvc.perform(get("/api/saved-routes/shared/{userId}", 99999L))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
