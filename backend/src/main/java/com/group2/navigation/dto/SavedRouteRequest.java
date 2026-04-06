package com.group2.navigation.dto;

import jakarta.validation.constraints.*;

public class SavedRouteRequest {

    @NotNull(message = "userId is required")
    @Min(value = 1, message = "userId must be a positive number")
    private Long userId;

    @NotBlank(message = "name is required")
    @Size(max = 200, message = "name must not exceed 200 characters")
    private String name;

    @Size(max = 300, message = "startAddress must not exceed 300 characters")
    private String startAddress;

    @Size(max = 300, message = "endAddress must not exceed 300 characters")
    private String endAddress;

    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;

    @DecimalMin(value = "0", message = "wheelchairWeight must be at least 0")
    @DecimalMax(value = "10", message = "wheelchairWeight must be at most 10")
    private double wheelchairWeight;

    @DecimalMin(value = "0", message = "crimeWeight must be at least 0")
    @DecimalMax(value = "10", message = "crimeWeight must be at most 10")
    private double crimeWeight;

    @DecimalMin(value = "0", message = "lightingWeight must be at least 0")
    @DecimalMax(value = "10", message = "lightingWeight must be at most 10")
    private double lightingWeight;

    @DecimalMin(value = "0", message = "constructionWeight must be at least 0")
    @DecimalMax(value = "10", message = "constructionWeight must be at most 10")
    private double constructionWeight;

    @Min(value = 0, message = "timeOfDay must be at least 0")
    @Max(value = 23, message = "timeOfDay must be at most 23")
    private int timeOfDay = 12;

    @DecimalMin(value = "0", message = "maxDistanceToHospital must be at least 0")
    @DecimalMax(value = "50000", message = "maxDistanceToHospital must be at most 50000")
    private double maxDistanceToHospital;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String sa) { this.startAddress = sa; }

    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String ea) { this.endAddress = ea; }

    public double getStartLat() { return startLat; }
    public void setStartLat(double v) { this.startLat = v; }

    public double getStartLng() { return startLng; }
    public void setStartLng(double v) { this.startLng = v; }

    public double getEndLat() { return endLat; }
    public void setEndLat(double v) { this.endLat = v; }

    public double getEndLng() { return endLng; }
    public void setEndLng(double v) { this.endLng = v; }

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
}
