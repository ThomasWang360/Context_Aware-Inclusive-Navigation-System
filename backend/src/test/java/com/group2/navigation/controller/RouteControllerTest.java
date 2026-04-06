package com.group2.navigation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.navigation.dto.CreateHealthServiceRequest;
import com.group2.navigation.model.HealthService;
import com.group2.navigation.model.ConstructionProject;
import com.group2.navigation.repository.ConstructionProjectRepository;
import com.group2.navigation.repository.HealthServiceRepository;
import org.junit.jupiter.api.*;
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
class RouteControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private HealthServiceRepository healthRepo;
    @Autowired private ConstructionProjectRepository constructionRepo;

    @BeforeEach
    void cleanDb() {
        healthRepo.deleteAll();
        constructionRepo.deleteAll();
    }

    // ── Health Check ────────────────────────────────────────────────────

    @Test
    void healthCheck_returnsOk() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.service").value("navigation-api"));
    }

    // ── Route Calculation ───────────────────────────────────────────────

    @Test
    void calculateRoute_withAddresses_returns200() throws Exception {
        String body = """
            {
              "startAddress": "CN Tower",
              "endAddress": "Union Station",
              "preferences": {
                "wheelchairWeight": 0,
                "crimeWeight": 0,
                "lightingWeight": 0,
                "constructionWeight": 0,
                "timeOfDay": 12,
                "maxDistanceToHospital": 0
              }
            }
            """;

        mvc.perform(post("/api/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").exists());
    }

    @Test
    void calculateRoute_withCoordinates_returns200() throws Exception {
        String body = """
            {
              "startLat": 43.6532,
              "startLng": -79.3832,
              "endLat": 43.6544,
              "endLng": -79.3807,
              "preferences": {
                "wheelchairWeight": 0,
                "crimeWeight": 0,
                "lightingWeight": 0,
                "constructionWeight": 0,
                "timeOfDay": 12,
                "maxDistanceToHospital": 0
              }
            }
            """;

        mvc.perform(post("/api/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").exists());
    }

    @Test
    void calculateRoute_noStartOrEnd_returns400() throws Exception {
        mvc.perform(post("/api/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void calculateRoute_getMethod_returns405() throws Exception {
        mvc.perform(get("/api/route"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void calculateRoute_isPost_notGet() throws Exception {
        // Verify the route endpoint accepts POST (not 405)
        int status = mvc.perform(post("/api/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"startAddress\":\"CN Tower\",\"endAddress\":\"Union Station\"}"))
            .andReturn().getResponse().getStatus();
        // May return 200 or 400 (if geocoding fails), but must not return 405
        org.assertj.core.api.Assertions.assertThat(status).isNotEqualTo(405);
    }

    // ── Geocode ─────────────────────────────────────────────────────────

    @Test
    void geocode_missingAddress_returns400() throws Exception {
        mvc.perform(get("/api/geocode"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.address").exists());
    }

    @Test
    void geocode_blankAddress_returns400() throws Exception {
        mvc.perform(get("/api/geocode").param("address", "  "))
            .andExpect(status().isBadRequest());
    }

    @Test
    void geocode_tooLongAddress_returns400() throws Exception {
        mvc.perform(get("/api/geocode").param("address", "x".repeat(201)))
            .andExpect(status().isBadRequest());
    }

    // ── Default Preferences ─────────────────────────────────────────────

    @Test
    void getDefaultPreferences_returns200() throws Exception {
        mvc.perform(get("/api/preferences"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wheelchairWeight").value(0.0))
            .andExpect(jsonPath("$.crimeWeight").value(0.0))
            .andExpect(jsonPath("$.timeOfDay").value(12));
    }

    // ── POIs ────────────────────────────────────────────────────────────

    @Test
    void getPOIs_validRequest_returns200() throws Exception {
        mvc.perform(get("/api/pois")
                .param("lat", "43.65")
                .param("lng", "-79.38"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pois").isArray())
            .andExpect(jsonPath("$.center.lat").value(43.65))
            .andExpect(jsonPath("$.radiusMeters").value(500.0));
    }

    @Test
    void getPOIs_withHospitalType_returnsOnlyHospitals() throws Exception {
        healthRepo.save(new HealthService("Test Hospital", "123 St",
                43.65, -79.38, "Full", "555-0100"));
        constructionRepo.save(new ConstructionProject("Road", "456 Ave",
                "Active", "6 months", 43.65, -79.38));

        mvc.perform(get("/api/pois")
                .param("lat", "43.65")
                .param("lng", "-79.38")
                .param("type", "hospital"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pois[*].type", everyItem(is("hospital"))));
    }

    @Test
    void getPOIs_withConstructionType_returnsOnlyConstruction() throws Exception {
        constructionRepo.save(new ConstructionProject("Road", "456 Ave",
                "Active", "6 months", 43.65, -79.38));

        mvc.perform(get("/api/pois")
                .param("lat", "43.65")
                .param("lng", "-79.38")
                .param("type", "construction"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pois[*].type", everyItem(is("construction"))));
    }

    @Test
    void getPOIs_invalidType_returns400() throws Exception {
        mvc.perform(get("/api/pois")
                .param("lat", "43.65")
                .param("lng", "-79.38")
                .param("type", "invalid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(
                "Invalid POI type. Allowed values: hospital, construction"));
    }

    @Test
    void getPOIs_missingLat_returns400() throws Exception {
        mvc.perform(get("/api/pois").param("lng", "-79.38"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPOIs_invalidLat_returns400() throws Exception {
        mvc.perform(get("/api/pois")
                .param("lat", "91")
                .param("lng", "-79.38"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPOIs_radiusTooLarge_returns400() throws Exception {
        mvc.perform(get("/api/pois")
                .param("lat", "43.65")
                .param("lng", "-79.38")
                .param("radius", "20000"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getPOIs_customRadius_used() throws Exception {
        mvc.perform(get("/api/pois")
                .param("lat", "43.65")
                .param("lng", "-79.38")
                .param("radius", "1000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.radiusMeters").value(1000.0));
    }

    // ── Create Health POI ───────────────────────────────────────────────

    @Test
    void createHealthPoi_validRequest_returns200() throws Exception {
        CreateHealthServiceRequest req = new CreateHealthServiceRequest();
        req.setAgencyName("New Hospital");
        req.setAddress("100 University Ave");
        req.setLatitude(43.65);
        req.setLongitude(-79.39);
        req.setAccessibility("Full");
        req.setPhone("416-555-0100");

        mvc.perform(post("/api/pois/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.agencyName").value("New Hospital"));
    }

    @Test
    void createHealthPoi_missingName_returns400() throws Exception {
        CreateHealthServiceRequest req = new CreateHealthServiceRequest();
        req.setLatitude(43.65);
        req.setLongitude(-79.39);

        mvc.perform(post("/api/pois/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // ─── Health Service UPDATE ─────────────────────────────────────

    @Test
    void updateHealthPoi_validRequest_returnsUpdated() throws Exception {
        // Create first
        CreateHealthServiceRequest create = new CreateHealthServiceRequest();
        create.setAgencyName("Original Hospital");
        create.setAddress("100 University Ave");
        create.setLatitude(43.65);
        create.setLongitude(-79.39);
        create.setAccessibility("Partial");
        create.setPhone("416-555-0100");

        String createResult = mvc.perform(post("/api/pois/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(create)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long poiId = mapper.readTree(createResult).get("id").asLong();

        // Update
        CreateHealthServiceRequest update = new CreateHealthServiceRequest();
        update.setAgencyName("Renamed Hospital");
        update.setAddress("200 King St");
        update.setLatitude(43.66);
        update.setLongitude(-79.40);
        update.setAccessibility("Full");
        update.setPhone("416-555-0200");

        mvc.perform(put("/api/pois/health/{id}", poiId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.agencyName").value("Renamed Hospital"))
            .andExpect(jsonPath("$.address").value("200 King St"));
    }

    @Test
    void updateHealthPoi_notFound_returns400() throws Exception {
        CreateHealthServiceRequest update = new CreateHealthServiceRequest();
        update.setAgencyName("Ghost Hospital");
        update.setLatitude(43.65);
        update.setLongitude(-79.39);

        mvc.perform(put("/api/pois/health/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    // ─── Health Service DELETE ─────────────────────────────────────

    @Test
    void deleteHealthPoi_existing_returns204() throws Exception {
        CreateHealthServiceRequest create = new CreateHealthServiceRequest();
        create.setAgencyName("To Delete");
        create.setLatitude(43.65);
        create.setLongitude(-79.39);

        String createResult = mvc.perform(post("/api/pois/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(create)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long poiId = mapper.readTree(createResult).get("id").asLong();

        mvc.perform(delete("/api/pois/health/{id}", poiId))
            .andExpect(status().isNoContent());
    }
}
