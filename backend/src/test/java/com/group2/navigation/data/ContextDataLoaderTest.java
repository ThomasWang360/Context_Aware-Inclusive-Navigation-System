package com.group2.navigation.data;

import com.group2.navigation.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ContextDataLoaderTest {

    @Autowired
    private ContextDataLoader loader;

    @Autowired
    private CrimeIncidentRepository crimeRepo;

    @Autowired
    private StreetLightRepository lightRepo;

    @Autowired
    private ConstructionProjectRepository constructionRepo;

    @Autowired
    private HealthServiceRepository healthRepo;

    @Autowired
    private CyclingSegmentRepository cyclingRepo;

    // ------------------------------------------------------------------
    // Crime data tests
    // ------------------------------------------------------------------

    @Test
    void loadCrimeData_validCsv_loadsRecords() throws IOException {
        long before = crimeRepo.count();
        File csv = createTempCsv("crime_test.csv",
                "EVENT_UNIQUE_ID,OCC_YEAR,OFFENCE,CSI_CATEGORY,LAT_WGS84,LONG_WGS84,HOOD_158,NEIGHBOURHOOD_158",
                "E1,2023,Assault,Crimes Against the Person,43.7,-79.4,001,Downtown");
        loader.loadCrimeData(csv.getAbsolutePath());
        assertThat(crimeRepo.count()).isGreaterThan(before);
        csv.delete();
    }

    @Test
    void loadCrimeData_missingCoords_skipsRecord() throws IOException {
        long before = crimeRepo.count();
        File csv = createTempCsv("crime_nocoord.csv",
                "EVENT_UNIQUE_ID,OCC_YEAR,OFFENCE,CSI_CATEGORY,LAT_WGS84,LONG_WGS84,HOOD_158,NEIGHBOURHOOD_158",
                "E2,2023,Theft,Property,,, ,Missing");
        loader.loadCrimeData(csv.getAbsolutePath());
        assertThat(crimeRepo.count()).isEqualTo(before);
        csv.delete();
    }

    @Test
    void loadCrimeData_zeroCoords_skipsRecord() throws IOException {
        long before = crimeRepo.count();
        File csv = createTempCsv("crime_zero.csv",
                "EVENT_UNIQUE_ID,OCC_YEAR,OFFENCE,CSI_CATEGORY,LAT_WGS84,LONG_WGS84,HOOD_158,NEIGHBOURHOOD_158",
                "E3,2023,Robbery,Property,0.0,0.0,002,Zero");
        loader.loadCrimeData(csv.getAbsolutePath());
        assertThat(crimeRepo.count()).isEqualTo(before);
        csv.delete();
    }

    @Test
    void loadCrimeData_invalidPath_doesNotCrash() {
        assertThatNoException().isThrownBy(() ->
                loader.loadCrimeData("/nonexistent/path.csv"));
    }

    @Test
    void loadCrimeData_badYear_skipsRecord() throws IOException {
        long before = crimeRepo.count();
        File csv = createTempCsv("crime_badyear.csv",
                "EVENT_UNIQUE_ID,OCC_YEAR,OFFENCE,CSI_CATEGORY,LAT_WGS84,LONG_WGS84,HOOD_158,NEIGHBOURHOOD_158",
                "E4,notAYear,Assault,CCI,43.7,-79.4,003,BadYear");
        loader.loadCrimeData(csv.getAbsolutePath());
        assertThat(crimeRepo.count()).isEqualTo(before);
        csv.delete();
    }

    // ------------------------------------------------------------------
    // Street light data tests
    // ------------------------------------------------------------------

    @Test
    void loadStreetLightData_validCsv_loadsRecords() throws IOException {
        long before = lightRepo.count();
        File csv = createTempCsv("light_test.csv",
                "SUBTYPE_CODE,geometry",
                "6006,\"{\"\"coordinates\"\": [[-79.4, 43.7]], \"\"type\"\": \"\"MultiPoint\"\"}\"");
        loader.loadStreetLightData(csv.getAbsolutePath());
        assertThat(lightRepo.count()).isGreaterThan(before);
        csv.delete();
    }

    @Test
    void loadStreetLightData_nonStreetLightCode_skips() throws IOException {
        long before = lightRepo.count();
        File csv = createTempCsv("light_wrong.csv",
                "SUBTYPE_CODE,geometry",
                "9999,\"{\"\"coordinates\"\": [[-79.4, 43.7]], \"\"type\"\": \"\"MultiPoint\"\"}\"");
        loader.loadStreetLightData(csv.getAbsolutePath());
        assertThat(lightRepo.count()).isEqualTo(before);
        csv.delete();
    }

    @Test
    void loadStreetLightData_invalidPath_doesNotCrash() {
        assertThatNoException().isThrownBy(() ->
                loader.loadStreetLightData("/nonexistent/path.csv"));
    }

    // ------------------------------------------------------------------
    // Health data tests
    // ------------------------------------------------------------------

    @Test
    void loadHealthData_validCsv_loadsRecords() throws IOException {
        long before = healthRepo.count();
        File csv = createTempCsv("health_test.csv",
                "AGENCY_NAME,ORGANIZATION_ADDRESS,LATITUDE,LONGITUDE,ACCESSIBILITY,OFFICE_PHONE",
                "TestClinic,123 Main,43.65,-79.38,Full,416-555-0000");
        loader.loadHealthData(csv.getAbsolutePath());
        assertThat(healthRepo.count()).isGreaterThan(before);
        csv.delete();
    }

    @Test
    void loadHealthData_missingCoords_skips() throws IOException {
        long before = healthRepo.count();
        File csv = createTempCsv("health_nocoord.csv",
                "AGENCY_NAME,ORGANIZATION_ADDRESS,LATITUDE,LONGITUDE,ACCESSIBILITY,OFFICE_PHONE",
                "Clinic2,456 Elm,,,None,416-555-1111");
        loader.loadHealthData(csv.getAbsolutePath());
        assertThat(healthRepo.count()).isEqualTo(before);
        csv.delete();
    }

    @Test
    void loadHealthData_invalidPath_doesNotCrash() {
        assertThatNoException().isThrownBy(() ->
                loader.loadHealthData("/nonexistent/path.csv"));
    }

    // ------------------------------------------------------------------
    // Construction data tests
    // ------------------------------------------------------------------

    @Test
    void loadConstructionData_validCsv_loadsRecords() throws IOException {
        long before = constructionRepo.count();
        File csv = createTempCsv("construction_test.csv",
                "PROJECT,LOCATION,STATUS,DURATION/ Construction Timeline,geometry",
                "P1,Queen St,Active,2024,\"{\"\"coordinates\"\": [[[-79.38, 43.65], [-79.39, 43.66]]], \"\"type\"\": \"\"MultiLineString\"\"}\"");
        loader.loadConstructionData(csv.getAbsolutePath());
        assertThat(constructionRepo.count()).isGreaterThan(before);
        csv.delete();
    }

    @Test
    void loadConstructionData_invalidPath_doesNotCrash() {
        assertThatNoException().isThrownBy(() ->
                loader.loadConstructionData("/nonexistent/path.csv"));
    }

    // ------------------------------------------------------------------
    // Cycling data tests
    // ------------------------------------------------------------------

    @Test
    void loadCyclingData_validCsv_loadsRecords() throws IOException {
        long before = cyclingRepo.count();
        File csv = createTempCsv("cycling_test.csv",
                "STREET_NAME,INFRA_LOWORDER,SURFACE,geometry",
                "Bloor,Bike Lane,Asphalt,\"{\"\"coordinates\"\": [[[-79.4, 43.66], [-79.41, 43.67]]], \"\"type\"\": \"\"MultiLineString\"\"}\"");
        loader.loadCyclingData(csv.getAbsolutePath());
        assertThat(cyclingRepo.count()).isGreaterThan(before);
        csv.delete();
    }

    @Test
    void loadCyclingData_invalidPath_doesNotCrash() {
        assertThatNoException().isThrownBy(() ->
                loader.loadCyclingData("/nonexistent/path.csv"));
    }

    // ------------------------------------------------------------------
    // loadAllData tests
    // ------------------------------------------------------------------

    @Test
    void loadAllData_doesNotCrash() {
        // loadAllData checks counts and skips already-loaded tables
        assertThatNoException().isThrownBy(() -> loader.loadAllData());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private File createTempCsv(String name, String header, String... rows) throws IOException {
        File file = File.createTempFile(name, ".csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(header);
            for (String row : rows) {
                pw.println(row);
            }
        }
        return file;
    }
}
