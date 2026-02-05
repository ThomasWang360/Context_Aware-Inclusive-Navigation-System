package com.group2.navigation.data;

import org.springframework.stereotype.Component;

/**
 * Loads context data from CSV files into the database.
 * 
 * TODO (Arshia - lead, Andrew, Simon, Maria):
 * - Implement CSV parsing for each data type
 * - Add data validation
 * - Create scheduled job to refresh data periodically
 */
@Component
public class ContextDataLoader {
    
    /**
     * Load crime data from Toronto Police Open Data CSV.
     * 
     * Data source: https://data.torontopolice.on.ca/
     * 
     * TODO: Implement this
     */
    public void loadCrimeData(String csvPath) {
        System.out.println("TODO: Load crime data from " + csvPath);
        // Steps:
        // 1. Read CSV file
        // 2. Parse lat/lng and crime type
        // 3. Aggregate into grid cells or neighborhoods
        // 4. Store in H2 database
    }
    
    /**
     * Load street light data from Toronto Open Data.
     * 
     * Data source: https://open.toronto.ca/dataset/street-lighting/
     * 
     * TODO: Implement this
     */
    public void loadStreetLightData(String csvPath) {
        System.out.println("TODO: Load street light data from " + csvPath);
        // Steps:
        // 1. Read CSV/JSON file
        // 2. Extract light pole locations
        // 3. Store in H2 database with spatial indexing
    }
    
    /**
     * Load construction/road closure data.
     * 
     * Data source: https://open.toronto.ca/dataset/road-restrictions/
     * 
     * TODO: Implement this
     */
    public void loadConstructionData(String csvPath) {
        System.out.println("TODO: Load construction data from " + csvPath);
        // Steps:
        // 1. Read CSV file
        // 2. Parse location and date range
        // 3. Filter for active construction
        // 4. Store in H2 database
    }
    
    /**
     * Load all data files at startup.
     * 
     * TODO: Call this from application startup
     */
    public void loadAllData() {
        // TODO: Implement data loading
        // loadCrimeData("data/raw/crime_data.csv");
        // loadStreetLightData("data/raw/street_lights.csv");
        // loadConstructionData("data/raw/construction.csv");
    }
}
