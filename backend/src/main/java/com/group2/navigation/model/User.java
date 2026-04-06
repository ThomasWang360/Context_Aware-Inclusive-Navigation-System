package com.group2.navigation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registered user account.
 * Stores login credentials and the user's saved route preferences,
 * so they don't have to re-enter them every time they open the app.
 *
 * Preferences are embedded directly in this table to keep the prototype simple
 * (no separate preferences table / join needed).
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt hash

    @Column(name = "display_name")
    private String displayName;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 200)
    private String location;

    // --- relationships ---

    @JsonIgnore
    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<Message> sentMessages = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
    private List<Message> receivedMessages = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedRoute> savedRoutes = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "sharedWith", fetch = FetchType.LAZY)
    private Set<SavedRoute> sharedRoutes = new HashSet<>();

    // --- saved preferences (same fields as UserPreferences) ---

    private double wheelchairWeight;     // 0-10
    private double crimeWeight;          // 0-10
    private double lightingWeight;       // 0-10
    private double constructionWeight;   // 0-10
    private int timeOfDay;               // 0-23
    private double maxDistanceToHospital; // meters, 0 = don't care

    public User() {
        this.timeOfDay = 12;
    }

    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.timeOfDay = 12;
    }

    public User(String username, String password, String displayName, String location) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.location = location;
        this.timeOfDay = 12;
    }

    /** Build a UserPreferences object from this user's saved settings. */
    public UserPreferences toPreferences() {
        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(wheelchairWeight);
        prefs.setCrimeWeight(crimeWeight);
        prefs.setLightingWeight(lightingWeight);
        prefs.setConstructionWeight(constructionWeight);
        prefs.setTimeOfDay(timeOfDay);
        prefs.setMaxDistanceToHospital(maxDistanceToHospital);
        return prefs;
    }

    /** Overwrite this user's saved settings from a UserPreferences object. */
    public void applyPreferences(UserPreferences prefs) {
        this.wheelchairWeight = prefs.getWheelchairWeight();
        this.crimeWeight = prefs.getCrimeWeight();
        this.lightingWeight = prefs.getLightingWeight();
        this.constructionWeight = prefs.getConstructionWeight();
        this.timeOfDay = prefs.getTimeOfDay();
        this.maxDistanceToHospital = prefs.getMaxDistanceToHospital();
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

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

    public List<Message> getSentMessages() { return sentMessages; }
    public void setSentMessages(List<Message> sentMessages) { this.sentMessages = sentMessages; }

    public List<Message> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(List<Message> receivedMessages) { this.receivedMessages = receivedMessages; }

    public List<SavedRoute> getSavedRoutes() { return savedRoutes; }
    public void setSavedRoutes(List<SavedRoute> savedRoutes) { this.savedRoutes = savedRoutes; }

    public Set<SavedRoute> getSharedRoutes() { return sharedRoutes; }
    public void setSharedRoutes(Set<SavedRoute> sharedRoutes) { this.sharedRoutes = sharedRoutes; }
}
