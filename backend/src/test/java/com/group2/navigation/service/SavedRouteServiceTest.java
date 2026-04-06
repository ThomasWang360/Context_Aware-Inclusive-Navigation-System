package com.group2.navigation.service;

import com.group2.navigation.dto.SavedRouteRequest;
import com.group2.navigation.model.User;
import com.group2.navigation.repository.MessageRepository;
import com.group2.navigation.repository.SavedRouteRepository;
import com.group2.navigation.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SavedRouteServiceTest {

    @Autowired private SavedRouteService savedRouteService;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepo;
    @Autowired private SavedRouteRepository routeRepo;
    @Autowired private MessageRepository messageRepo;

    private User testUser;

    @BeforeEach
    void setUp() {
        messageRepo.deleteAll();
        routeRepo.deleteAll();
        userRepo.deleteAll();
        testUser = authService.signup("routeuser", "password123", "Route User", "Toronto");
    }

    private SavedRouteRequest makeRequest(Long userId, String name) {
        SavedRouteRequest req = new SavedRouteRequest();
        req.setUserId(userId);
        req.setName(name);
        req.setStartAddress("CN Tower");
        req.setEndAddress("Union Station");
        req.setStartLat(43.6426);
        req.setStartLng(-79.3871);
        req.setEndLat(43.6453);
        req.setEndLng(-79.3806);
        req.setWheelchairWeight(5);
        req.setCrimeWeight(3);
        req.setLightingWeight(2);
        req.setConstructionWeight(1);
        req.setTimeOfDay(14);
        req.setMaxDistanceToHospital(3000);
        return req;
    }

    // --- create ---

    @Test
    void create_validRequest_returnsMap() {
        Map<String, Object> result = savedRouteService.create(makeRequest(testUser.getId(), "My Route"));

        assertThat(result).containsKey("id");
        assertThat(result.get("name")).isEqualTo("My Route");
        assertThat(result.get("userId")).isEqualTo(testUser.getId());
        assertThat(result.get("startAddress")).isEqualTo("CN Tower");
        assertThat(result.get("endAddress")).isEqualTo("Union Station");
        assertThat(result.get("wheelchairWeight")).isEqualTo(5.0);
        assertThat(result.get("crimeWeight")).isEqualTo(3.0);
        assertThat(result.get("timeOfDay")).isEqualTo(14);
        assertThat(result.get("createdAt")).isNotNull();
    }

    @Test
    void create_trimsName() {
        Map<String, Object> result = savedRouteService.create(makeRequest(testUser.getId(), "  Padded Name  "));
        assertThat(result.get("name")).isEqualTo("Padded Name");
    }

    @Test
    void create_trimsAddresses() {
        SavedRouteRequest req = makeRequest(testUser.getId(), "Trimmed");
        req.setStartAddress("  CN Tower  ");
        req.setEndAddress("  Union  ");

        Map<String, Object> result = savedRouteService.create(req);
        assertThat(result.get("startAddress")).isEqualTo("CN Tower");
        assertThat(result.get("endAddress")).isEqualTo("Union");
    }

    @Test
    void create_nonexistentUser_throws() {
        assertThatThrownBy(() -> savedRouteService.create(makeRequest(99999L, "Test")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void create_exceedsMaxRoutes_throws() {
        for (int i = 0; i < 20; i++) {
            savedRouteService.create(makeRequest(testUser.getId(), "Route " + i));
        }

        assertThatThrownBy(() -> savedRouteService.create(makeRequest(testUser.getId(), "Route 21")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum");
    }

    @Test
    void create_exactly20Routes_succeeds() {
        for (int i = 0; i < 20; i++) {
            savedRouteService.create(makeRequest(testUser.getId(), "Route " + i));
        }
        List<Map<String, Object>> routes = savedRouteService.findByUser(testUser.getId());
        assertThat(routes).hasSize(20);
    }

    @Test
    void create_nullAddresses_setsNull() {
        SavedRouteRequest req = makeRequest(testUser.getId(), "No Addresses");
        req.setStartAddress(null);
        req.setEndAddress(null);

        Map<String, Object> result = savedRouteService.create(req);
        assertThat(result.get("startAddress")).isNull();
        assertThat(result.get("endAddress")).isNull();
    }

    // --- findByUser ---

    @Test
    void findByUser_withRoutes_returnsList() {
        savedRouteService.create(makeRequest(testUser.getId(), "Route A"));
        savedRouteService.create(makeRequest(testUser.getId(), "Route B"));

        List<Map<String, Object>> routes = savedRouteService.findByUser(testUser.getId());
        assertThat(routes).hasSize(2);
    }

    @Test
    void findByUser_noRoutes_returnsEmptyList() {
        List<Map<String, Object>> routes = savedRouteService.findByUser(testUser.getId());
        assertThat(routes).isEmpty();
    }

    @Test
    void findByUser_nonexistentUser_throws() {
        assertThatThrownBy(() -> savedRouteService.findByUser(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void findByUser_orderedByCreatedAtDesc() {
        savedRouteService.create(makeRequest(testUser.getId(), "First"));
        savedRouteService.create(makeRequest(testUser.getId(), "Second"));
        savedRouteService.create(makeRequest(testUser.getId(), "Third"));

        List<Map<String, Object>> routes = savedRouteService.findByUser(testUser.getId());
        assertThat(routes).hasSize(3);
        // Most recent first
        assertThat(routes.get(0).get("name")).isEqualTo("Third");
        assertThat(routes.get(2).get("name")).isEqualTo("First");
    }

    @Test
    void findByUser_onlyReturnsOwnRoutes() {
        User other = authService.signup("otheruser", "password123", "Other", null);

        savedRouteService.create(makeRequest(testUser.getId(), "My Route"));
        savedRouteService.create(makeRequest(other.getId(), "Their Route"));

        List<Map<String, Object>> myRoutes = savedRouteService.findByUser(testUser.getId());
        assertThat(myRoutes).hasSize(1);
        assertThat(myRoutes.get(0).get("name")).isEqualTo("My Route");
    }

    // --- update ---

    @Test
    void update_validRequest_updatesFields() {
        Map<String, Object> created = savedRouteService.create(makeRequest(testUser.getId(), "Original"));
        Long routeId = ((Number) created.get("id")).longValue();

        SavedRouteRequest updateReq = makeRequest(testUser.getId(), "Updated Name");
        updateReq.setWheelchairWeight(10);
        updateReq.setCrimeWeight(0);

        Map<String, Object> updated = savedRouteService.update(routeId, updateReq);
        assertThat(updated.get("name")).isEqualTo("Updated Name");
        assertThat(updated.get("wheelchairWeight")).isEqualTo(10.0);
        assertThat(updated.get("crimeWeight")).isEqualTo(0.0);
    }

    @Test
    void update_nonexistentRoute_throws() {
        SavedRouteRequest req = makeRequest(testUser.getId(), "Ghost");
        assertThatThrownBy(() -> savedRouteService.update(99999L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saved route not found");
    }

    @Test
    void update_wrongUser_throws() {
        Map<String, Object> created = savedRouteService.create(makeRequest(testUser.getId(), "Mine"));
        Long routeId = ((Number) created.get("id")).longValue();

        User other = authService.signup("intruder", "password123", "Intruder", null);
        SavedRouteRequest req = makeRequest(other.getId(), "Hijacked");

        assertThatThrownBy(() -> savedRouteService.update(routeId, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Route does not belong to this user");
    }

    // --- delete ---

    @Test
    void delete_validOwner_removesRoute() {
        Map<String, Object> created = savedRouteService.create(makeRequest(testUser.getId(), "ToDelete"));
        Long routeId = ((Number) created.get("id")).longValue();

        savedRouteService.delete(routeId, testUser.getId());

        List<Map<String, Object>> routes = savedRouteService.findByUser(testUser.getId());
        assertThat(routes).isEmpty();
    }

    @Test
    void delete_nonexistentRoute_throws() {
        assertThatThrownBy(() -> savedRouteService.delete(99999L, testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saved route not found");
    }

    @Test
    void delete_wrongUser_throws() {
        Map<String, Object> created = savedRouteService.create(makeRequest(testUser.getId(), "Protected"));
        Long routeId = ((Number) created.get("id")).longValue();

        User other = authService.signup("thief", "password123", "Thief", null);

        assertThatThrownBy(() -> savedRouteService.delete(routeId, other.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Route does not belong to this user");
    }

    @Test
    void delete_onlyDeletesTargetRoute() {
        savedRouteService.create(makeRequest(testUser.getId(), "Keep"));
        Map<String, Object> toDelete = savedRouteService.create(makeRequest(testUser.getId(), "Remove"));
        Long routeId = ((Number) toDelete.get("id")).longValue();

        savedRouteService.delete(routeId, testUser.getId());

        List<Map<String, Object>> routes = savedRouteService.findByUser(testUser.getId());
        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).get("name")).isEqualTo("Keep");
    }

    // --- toMap ---

    @Test
    void toMap_containsAllFields() {
        Map<String, Object> result = savedRouteService.create(makeRequest(testUser.getId(), "Full"));

        assertThat(result).containsKeys("id", "userId", "name", "startAddress", "endAddress",
                "startLat", "startLng", "endLat", "endLng",
                "wheelchairWeight", "crimeWeight", "lightingWeight", "constructionWeight",
                "timeOfDay", "maxDistanceToHospital", "createdAt");
    }

    @Test
    void toMap_preservesCoordinates() {
        SavedRouteRequest req = makeRequest(testUser.getId(), "Coords");
        req.setStartLat(43.6532);
        req.setStartLng(-79.3832);
        req.setEndLat(43.6544);
        req.setEndLng(-79.3807);

        Map<String, Object> result = savedRouteService.create(req);
        assertThat((double) result.get("startLat")).isEqualTo(43.6532);
        assertThat((double) result.get("startLng")).isEqualTo(-79.3832);
        assertThat((double) result.get("endLat")).isEqualTo(43.6544);
        assertThat((double) result.get("endLng")).isEqualTo(-79.3807);
    }
}
