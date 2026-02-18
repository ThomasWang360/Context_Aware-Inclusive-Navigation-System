classDiagram
    class USER {
        - id: Long
        - email: String
        - passwordHash: String
        - name: String
        - createdAt: DateTime
        - lastLogin: DateTime
        - lastLogout: DateTime
        + User(email: String, name: String, password: String) void
        + getId() Long
        + setId(id: Long) void
        + getEmail() String
        + setEmail(email: String) void
        + getName() String
        + setName(name: String) void
        + setPassword(password: String) void
        + login() DateTime
        + logout() DateTime
        + getCreatedAt() DateTime
        + getLastLogin() DateTime
        + getLastLogout() DateTime
        + hashPassword(password: String) String
        + verifyPassword(password: String) Boolean
    }

    class USER_PREFERENCES {
        - id: Long
        - userId: Long
        - wheelchairAccessible: Boolean
        - avoidHighCrime: Boolean
        - preferLitStreets: Boolean
        - avoidConstruction: Boolean
        - avoidNoise: Boolean
        - preferredTimeOfDay: Int
        - maxDistanceToHospital: Double
        - walkingSpeed: Double
        - updatedAt: DateTime
        + UserPreferences(userId: Long) void
        + getId() Long
        + setId(id: Long) void
        + getUserId() Long
        + isWheelchairAccessible() Boolean
        + setWheelchairAccessible(value: Boolean) void
        + isAvoidHighCrime() Boolean
        + setAvoidHighCrime(value: Boolean) void
        + isPreferLitStreets() Boolean
        + setPreferLitStreets(value: Boolean) void
        + isAvoidConstruction() Boolean
        + setAvoidConstruction(value: Boolean) void
        + isAvoidNoise() Boolean
        + setAvoidNoise(value: Boolean) void
        + getPreferredTimeOfDay() Int
        + setPreferredTimeOfDay(value: Int) void
        + getMaxDistanceToHospital() Double
        + setMaxDistanceToHospital(value: Double) void
        + getWalkingSpeed() Double
        + setWalkingSpeed(value: Double) void
    }

    class CRIME_ZONE {
        - id: Long
        - latitude: Double
        - longitude: Double
        - gridSize: Double
        - crimeScore: Double
        - crimeType: String
        - incidentCount: Int
        - periodStart: DateTime
        - periodEnd: DateTime
        - lastUpdated: DateTime
        + getId() Long
        + setId(id: Long) void
        + getLatitude() Double
        + getLongitude() Double
        + getCrimeScore() Double
        + getCrimeType() String
        + getIncidentCount() Int
        + getPeriodStart() DateTime
        + getPeriodEnd() DateTime
        + getLastUpdated() DateTime
    }

    class STREET_LIGHT {
        - id: Long
        - latitude: Double
        - longitude: Double
        - coverageRadius: Double
        - lightType: String
        - isOperational: Boolean
        - installedDate: DateTime
        + getId() Long
        + setId(id: Long) void
        + getLatitude() Double
        + getLongitude() Double
        + getCoverageRadius() Double
        + getLightType() String
        + isOperational() Boolean
        + getInstalledDate() DateTime
    }

    class CONSTRUCTION {
        - id: Long
        - latitude: Double
        - longitude: Double
        - radius: Double
        - description: String
        - restrictionType: String
        - startDate: Date
        - endDate: Date
        - isActive: Boolean
        - lastUpdated: DateTime
        + getId() Long
        + setId(id: Long) void
        + getLatitude() Double
        + getLongitude() Double
        + getRadius() Double
        + getDescription() String
        + getRestrictionType() String
        + getStartDate() Date
        + getEndDate() Date
        + isActive() Boolean
        + getLastUpdated() DateTime
    }

    class HOSPITAL {
        - id: Long
        - name: String
        - latitude: Double
        - longitude: Double
        - address: String
        - phoneNumber: String
        - hasEmergency: Boolean
        - isOpen24Hours: Boolean
        + getId() Long
        + setId(id: Long) void
        + getName() String
        + getLatitude() Double
        + getLongitude() Double
        + getAddress() String
        + getPhoneNumber() String
        + hasEmergency() Boolean
        + isOpen24Hours() Boolean
    }

    class POI {
        - id: Long
        - name: String
        - type: String
        - latitude: Double
        - longitude: Double
        - address: String
        - openingHours: String
        - wheelchairAccessible: Boolean
        + getId() Long
        + setId(id: Long) void
        + getName() String
        + getType() String
        + getLatitude() Double
        + getLongitude() Double
        + getAddress() String
        + getOpeningHours() String
        + isWheelchairAccessible() Boolean
    }

    class ROAD_SEGMENT {
        - id: Long
        - osmWayId: Long
        - startLat: Double
        - startLng: Double
        - endLat: Double
        - endLng: Double
        - distance: Double
        - highwayType: String
        - surfaceType: String
        - wheelchairAccessible: Boolean
        - hasSidewalk: Boolean
        - isBikeLane: Boolean
        - isLit: Boolean
        - crimeScore: Double
        - hasConstruction: Boolean
        + getId() Long
        + setId(id: Long) void
        + getOsmWayId() Long
        + getDistance() Double
        + getHighwayType() String
        + getSurfaceType() String
        + isWheelchairAccessible() Boolean
        + hasSidewalk() Boolean
        + isBikeLane() Boolean
        + isLit() Boolean
        + getCrimeScore() Double
        + hasConstruction() Boolean
    }

    class WHEELCHAIR_ACCESSIBILITY {
        - id: Long
        - latitude: Double
        - longitude: Double
        - featureType: String
        - hasCurbCut: Boolean
        - hasRamp: Boolean
        - slopeGrade: Double
        - surfaceCondition: String
        - reportedAt: DateTime
        + getId() Long
        + setId(id: Long) void
        + getLatitude() Double
        + getLongitude() Double
        + getFeatureType() String
        + hasCurbCut() Boolean
        + hasRamp() Boolean
        + getSlopeGrade() Double
        + getSurfaceCondition() String
        + getReportedAt() DateTime
    }

    class BIKE_LANE {
        - id: Long
        - osmWayId: Long
        - laneType: String
        - startLat: Double
        - startLng: Double
        - endLat: Double
        - endLng: Double
        - isProtected: Boolean
        - surfaceType: String
        + getId() Long
        + setId(id: Long) void
        + getOsmWayId() Long
        + getLaneType() String
        + isProtected() Boolean
        + getSurfaceType() String
    }

    class NOISE_ZONE {
        - id: Long
        - latitude: Double
        - longitude: Double
        - radius: Double
        - noiseSource: String
        - noiseLevel: Int
        - activeFrom: DateTime
        - activeUntil: DateTime
        + getId() Long
        + setId(id: Long) void
        + getLatitude() Double
        + getLongitude() Double
        + getRadius() Double
        + getNoiseSource() String
        + getNoiseLevel() Int
        + getActiveFrom() DateTime
        + getActiveUntil() DateTime
    }

   class SAVED_ROUTE {
    - id: Long
    - userId: Long
    - name: String
    - startLat: Double
    - startLng: Double
    - endLat: Double
    - endLng: Double
    - routeCoordinatesJson: Text
    - /totalDistance: Double
    - /estimatedTime: Double
    - preferencesUsed: String
    - createdAt: DateTime

    + SavedRoute(userId: Long, name: String, startLat: Double, startLng: Double, endLat: Double, endLng: Double) void
    + getId() Long
    + setId(id: Long) void
    + getUserId() Long
    + getName() String
    + setName(name: String) void
    + calculateTotalDistance(startLat: Double, startLng: Double, endLat: Double, endLng: Double) Double
    + getTotalDistance() Double
    + calculateEstimatedTime(walkingSpeed: Double) Double
    + getEstimatedTime() Double
    + getRouteCoordinatesJson() Text
    + getPreferencesUsed() String
    + setPreferencesUsed(preferencesUsed: String) void
    + getCreatedAt() DateTime
}

    class ROUTE_HISTORY {
    - id: Long
    - userId: Long
    - startLat: Double
    - startLng: Double
    - endLat: Double
    - endLng: Double
    - /totalDistance: Double
    - /estimatedTime: Double
    - wheelchairUsed: Boolean
    - avoidCrimeUsed: Boolean
    - litStreetsUsed: Boolean
    - avoidConstructionUsed: Boolean
    - calculatedAt: DateTime

    + RouteHistory(userId: Long, startLat: Double, startLng: Double, endLat: Double, endLng: Double) void
    + getId() Long
    + setId(id: Long) void
    + getUserId() Long
    + calculateTotalDistance(startLat: Double, startLng: Double, endLat: Double, endLng: Double) Double
    + getTotalDistance() Double
    + calculateEstimatedTime(walkingSpeed: Double) Double
    + getEstimatedTime() Double
    + isWheelchairUsed() Boolean
    + isAvoidCrimeUsed() Boolean
    + isLitStreetsUsed() Boolean
    + isAvoidConstructionUsed() Boolean
    + getCalculatedAt() DateTime
}

    class FORUM_POST {
        - id: Long
        - userId: Long
        - title: String
        - content: Text
        - category: String
        - relatedLat: Double
        - relatedLng: Double
        - upvotes: Int
        - createdAt: DateTime
        - updatedAt: DateTime
    
        + ForumPost(userId: Long, title: String, content: Text, category: String, relatedLat: Double, relatedLng: Double) void
        + getId() Long
        + setId(id: Long) void
        + getUserId() Long
        + getTitle() String
        + setTitle(title: String) void
        + getContent() Text
        + setContent(content: Text) void
        + getCategory() String
        + setCategory(category: String) void
        + getUpvotes() Int
        + addUpvote() Int
        + getCreatedAt() DateTime
        + getUpdatedAt() DateTime
    }

    class FORUM_COMMENT {
        - id: Long
        - postId: Long
        - userId: Long
        - content: Text
        - upvotes: Int
        - createdAt: DateTime
        - updatedAt: DateTime

        + ForumComment(postId: Long, userId: Long, content: Text) void
        + getId() Long
        + setId(id: Long) void
        + getPostId() Long
        + getUserId() Long
        + getContent() Text
        + setContent(content: Text) void
        + getUpvotes() Int
        + addUpvote() Int
        + getCreatedAt() DateTime
        + getUpdatedAt() DateTime
    }

    class ACCESSIBILITY_REPORT {
        - id: Long
        - userId: Long
        - latitude: Double
        - longitude: Double
        - reportType: String
        - description: String
        - status: String
        - reportedAt: DateTime
        - resolvedAt: DateTime

        + AccessibilityReport(userId: Long, latitude: Double, longitude: Double, reportType: String) void
        + getId() Long
        + setId(id: Long) void
        + getUserId() Long
        + getLatitude() Double
        + getLongitude() Double
        + getReportType() String
        + setReportType(reportType: String) void
        + getDescription() String
        + setDescription(description: String) void
        + getStatus() String
        + setStatus(status: String) void
        + resolve(resolvedAt: DateTime) String
        + getReportedAt() DateTime
        + getResolvedAt() DateTime
       
    }

    class DATA_SOURCE {
        - id: Long
        - sourceName: String
        - sourceUrl: String
        - dataType: String
        - lastFetched: DateTime
        - nextScheduledFetch: DateTime

        + getId() Long
        + setId(id: Long) void
        + getSourceName() String
        + getSourceUrl() String
        + getDataType() String
        + getLastFetched() DateTime
        + getNextScheduledFetch() DateTime
        + fetch() void
        + scheduleFetch(nextFetch: DateTime) void
    }

    USER "1" --> "0..1" USER_PREFERENCES : has
    USER "1" --> "0..*" SAVED_ROUTE : saves
    USER "1" --> "0..*" ROUTE_HISTORY : generates
    USER "1" --> "0..*" FORUM_POST : creates
    USER "1" --> "0..*" FORUM_COMMENT : writes
    USER "1" --> "0..*" ACCESSIBILITY_REPORT : submits
    FORUM_POST "1" --> "0..*" FORUM_COMMENT : has