package com.group2.navigation.repository;

import com.group2.navigation.model.CrimeIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for querying crime incident data.
 * Uses bounding box queries to approximate spatial lookups.
 */
@Repository
public interface CrimeIncidentRepository extends JpaRepository<CrimeIncident, Long> {

    /**
     * Count crimes within a bounding box (approximate radius search).
     * Called by ContextDataRepository to compute a crime score for a location.
     */
    @Query("SELECT COUNT(c) FROM CrimeIncident c WHERE " +
           "c.latitude BETWEEN :minLat AND :maxLat AND " +
           "c.longitude BETWEEN :minLng AND :maxLng")
    long countInBoundingBox(@Param("minLat") double minLat,
                            @Param("maxLat") double maxLat,
                            @Param("minLng") double minLng,
                            @Param("maxLng") double maxLng);

    /**
     * Count recent crimes (last 3 years) within a bounding box.
     * More relevant for current safety assessment.
     */
    @Query("SELECT COUNT(c) FROM CrimeIncident c WHERE " +
           "c.latitude BETWEEN :minLat AND :maxLat AND " +
           "c.longitude BETWEEN :minLng AND :maxLng AND " +
           "c.occYear >= :sinceYear")
    long countRecentInBoundingBox(@Param("minLat") double minLat,
                                  @Param("maxLat") double maxLat,
                                  @Param("minLng") double minLng,
                                  @Param("maxLng") double maxLng,
                                  @Param("sinceYear") int sinceYear);

    @Query("SELECT c.latitude, c.longitude FROM CrimeIncident c WHERE " +
           "c.latitude BETWEEN :minLat AND :maxLat AND " +
           "c.longitude BETWEEN :minLng AND :maxLng AND " +
           "c.occYear >= :sinceYear")
    java.util.List<Object[]> findHeatmapPoints(@Param("minLat") double minLat,
                                                @Param("maxLat") double maxLat,
                                                @Param("minLng") double minLng,
                                                @Param("maxLng") double maxLng,
                                                @Param("sinceYear") int sinceYear);
}
