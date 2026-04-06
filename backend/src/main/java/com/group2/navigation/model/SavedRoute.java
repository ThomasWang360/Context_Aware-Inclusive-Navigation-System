package com.group2.navigation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A saved route belonging to a user.
 * Stores start/end locations and the user's preferred preferences at save time,
 * so they can re-run the route later with one click.
 */
@Entity
@Table(name = "saved_routes", indexes = {
    @Index(name = "idx_saved_route_user", columnList = "user_id")
})
public class SavedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "start_address", length = 300)
    private String startAddress;

    @Column(name = "end_address", length = 300)
    private String endAddress;

    @Column(name = "start_lat")
    private double startLat;

    @Column(name = "start_lng")
    private double startLng;

    @Column(name = "end_lat")
    private double endLat;

    @Column(name = "end_lng")
    private double endLng;

    private double wheelchairWeight;
    private double crimeWeight;
    private double lightingWeight;
    private double constructionWeight;
    private int timeOfDay;
    private double maxDistanceToHospital;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SavedRoute() {}

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }

    public double getStartLat() { return startLat; }
    public void setStartLat(double startLat) { this.startLat = startLat; }

    public double getStartLng() { return startLng; }
    public void setStartLng(double startLng) { this.startLng = startLng; }

    public double getEndLat() { return endLat; }
    public void setEndLat(double endLat) { this.endLat = endLat; }

    public double getEndLng() { return endLng; }
    public void setEndLng(double endLng) { this.endLng = endLng; }

    public double getWheelchairWeight() { return wheelchairWeight; }
    public void setWheelchairWeight(double v) { this.wheelchairWeight = v; }

    public double getCrimeWeight() { return crimeWeight; }
    public void setCrimeWeight(double v) { this.crimeWeight = v; }

    public double getLightingWeight() { return lightingWeight; }
    public void setLightingWeight(double v) { this.lightingWeight = v; }

    public double getConstructionWeight() { return constructionWeight; }
    public void setConstructionWeight(double v) { this.constructionWeight = v; }

    public int getTimeOfDay() { return timeOfDay; }
    public void setTimeOfDay(int v) { this.timeOfDay = v; }

    public double getMaxDistanceToHospital() { return maxDistanceToHospital; }
    public void setMaxDistanceToHospital(double v) { this.maxDistanceToHospital = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
