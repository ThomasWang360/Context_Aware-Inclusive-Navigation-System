# API Documentation

## Base URL

```
http://localhost:8080/api
```

---

## Authentication

### Sign Up

```
POST /auth/signup
```

**Request Body**

| Field       | Type   | Required | Description                |
|-------------|--------|----------|----------------------------|
| username    | String | Yes      | Unique username (3-50 chars) |
| password    | String | Yes      | Password (min 6 chars)     |
| displayName | String | Yes      | Display name (max 100 chars) |
| location    | String | No       | User's location (max 200 chars) |

**Responses**

| Status | Description |
|--------|-------------|
| 200    | `{ "id", "username", "displayName", "location", … }` |
| 400    | Validation errors or username already taken |

### Log In

```
POST /auth/login
```

**Request Body**

| Field    | Type   | Required | Description |
|----------|--------|----------|-------------|
| username | String | Yes      | Username    |
| password | String | Yes      | Password    |

**Responses**

| Status | Description |
|--------|-------------|
| 200    | User object |
| 401    | Invalid credentials |

### Get User

```
GET /auth/{userId}
```

### Update User Preferences

```
PUT /auth/{userId}/preferences
```

**Request Body** — `UserPreferences` object with fields: `wheelchairWeight`, `crimeWeight`, `lightingWeight`, `constructionWeight` (0-10), `timeOfDay` (0-23), `maxDistanceToHospital` (meters).

### Update Credentials

```
PUT /auth/{userId}/credentials
```

**Request Body**

| Field    | Type   | Required | Description |
|----------|--------|----------|-------------|
| email    | String | No       | New email   |
| password | String | No       | New password |

At least one field must be provided.

### Delete User

```
DELETE /auth/{userId}
```

Deletes the user and all associated messages and saved routes.

### Search Users

```
GET /auth/search?q={query}&excludeUserId={userId}
```

---

## Routing

### Calculate Route

```
POST /route
```

**Request Body**

| Field        | Type   | Required | Description                        |
|--------------|--------|----------|------------------------------------|
| startLat     | Double | Yes      | Start latitude                     |
| startLng     | Double | Yes      | Start longitude                    |
| endLat       | Double | Yes      | End latitude                       |
| endLng       | Double | Yes      | End longitude                      |
| startAddress | String | No       | Human-readable start address       |
| endAddress   | String | No       | Human-readable destination address |
| preferences  | Object | No       | UserPreferences for route scoring  |

**Response** — `RouteResponse` with `coordinates`, `distanceKm`, `estimatedMinutes`, `safetyScore`, etc.

### Geocode Address

```
GET /geocode?address={address}
```

Returns `{ "lat", "lng", "displayName" }` via Nominatim.

---

## Messages

### Get Inbox

```
GET /messages/{userId}
```

### Send Message

```
POST /messages
```

**Request Body**

| Field      | Type   | Required | Description             |
|------------|--------|----------|-------------------------|
| senderId   | Long   | Yes      | Sender's user ID        |
| receiverId | Long   | Yes      | Receiver's user ID      |
| content    | String | Yes      | Message text (max 4000) |

### Update Message

```
PUT /messages/{messageId}
```

**Request Body**

| Field   | Type   | Required | Description |
|---------|--------|----------|-------------|
| content | String | Yes      | New content |

### Delete Message

```
DELETE /messages/{messageId}
```

---

## Saved Routes

### Save a Route

```
POST /saved-routes
```

**Request Body**

| Field                 | Type   | Required | Description                  |
|-----------------------|--------|----------|------------------------------|
| userId                | Long   | Yes      | Owner user ID                |
| name                  | String | Yes      | Route name (max 200 chars)   |
| startAddress          | String | No       | Start address                |
| endAddress            | String | No       | End address                  |
| startLat / startLng   | Double | No       | Start coordinates            |
| endLat / endLng       | Double | No       | End coordinates              |
| wheelchairWeight      | Double | No       | 0-10                         |
| crimeWeight           | Double | No       | 0-10                         |
| lightingWeight        | Double | No       | 0-10                         |
| constructionWeight    | Double | No       | 0-10                         |
| timeOfDay             | Int    | No       | 0-23                         |
| maxDistanceToHospital | Double | No       | 0-50000 meters               |

**Response** — 201 Created with route object.  
Maximum 20 saved routes per user.

### Get User's Saved Routes

```
GET /saved-routes/{userId}
```

### Update a Saved Route

```
PUT /saved-routes/{routeId}
```

Request body same as create. The `userId` in the body must match the route's owner.

### Delete a Saved Route

```
DELETE /saved-routes/{routeId}?userId={userId}
```

---

## Points of Interest (POIs)

### Get Nearby Health Services

```
GET /pois/health?lat={lat}&lng={lng}&radiusKm={radius}
```

### Create Health Service

```
POST /pois/health
```

**Request Body**

| Field         | Type   | Required | Description       |
|---------------|--------|----------|-------------------|
| agencyName    | String | Yes      | Name (max 200)    |
| address       | String | No       | Street address    |
| latitude      | Double | Yes      | Latitude          |
| longitude     | Double | Yes      | Longitude         |
| accessibility | String | No       | Accessibility info |
| phone         | String | No       | Phone number      |

### Update Health Service

```
PUT /pois/health/{id}
```

Same body as create. Returns 400 if not found.

### Delete Health Service

```
DELETE /pois/health/{id}
```

### Get Nearby Crime Incidents

```
GET /pois/crime?lat={lat}&lng={lng}&radiusKm={radius}
```

### Get Nearby Construction

```
GET /pois/construction?lat={lat}&lng={lng}&radiusKm={radius}
```

### Get Nearby Street Lights

```
GET /pois/lights?lat={lat}&lng={lng}&radiusKm={radius}
```

---

## Entity Relationships

| Entity       | Relationships                                          |
|--------------|--------------------------------------------------------|
| User         | Has many Messages (sender), Messages (receiver), SavedRoutes |
| Message      | Belongs to User (sender), User (receiver)              |
| SavedRoute   | Belongs to User                                        |
| HealthService | Standalone (loaded from Toronto Open Data)            |
| CrimeIncident | Standalone (loaded from Toronto Open Data)            |
| ConstructionProject | Standalone (loaded from Toronto Open Data)       |
| StreetLight  | Standalone (loaded from Toronto Open Data)             |
| CyclingSegment | Standalone (loaded from Toronto Open Data)           |
