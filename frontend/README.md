# Frontend - Appsmith

We're using Appsmith for the frontend. It's a low-code platform that makes it easy to build UIs.

## Setup (Local Development)

### Option 1: Docker 
From the project root:
```bash
docker-compose up appsmith
```
Then go to http://localhost:80

### Option 2: Appsmith Cloud
1. Sign up at https://www.appsmith.com/
2. Create a new app
3. Import the app config (once we have it)

## Pages to Build

- **Map Page** (main) - Leaflet map with route display
- **Login/Register** - User accounts (stretch goal)
- **Preferences** - Save accessibility preferences
- **Forum** - Community features (stretch goal)

## Connecting to the Backend

In Appsmith, create an API datasource:
- URL: `http://localhost:8080` (or `http://backend:8080` in Docker)
- Headers: `Content-Type: application/json`

### API calls we need:

**Calculate Route:**
```javascript
POST /api/route
{
  "startLat": 43.6532,
  "startLng": -79.3832,
  "endLat": 43.6629,
  "endLng": -79.3957,
  "preferences": {
    "wheelchairAccessible": true,
    "avoidHighCrime": true,
    "preferLitStreets": true
  }
}
```

**Response:**
```javascript
{
  "coordinates": [[43.65, -79.38], [43.66, -79.39], ...],
  "totalDistance": 1234.5,
  "estimatedTime": 15.2,
  "success": true
}
```

## Leaflet Integration

We need to use a custom widget or iframe for the map since Appsmith doesn't have a built-in map component.

### Option A: Custom Widget (newer Appsmith)
Create a custom widget with Leaflet JS. See Appsmith docs.

### Option B: iframe
Create a separate HTML file with the map and embed it.

Example Leaflet code:
```javascript
var map = L.map('map').setView([43.65, -79.38], 13);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: 'OpenStreetMap'
}).addTo(map);

// Draw route from API response
function drawRoute(coordinates) {
    L.polyline(coordinates, {color: 'blue'}).addTo(map);
}
```

## Who's Working on This

| Person | Task |
|--------|------|
| Dooyeon (lead) | Appsmith layout, page design |
| Ivan | Appsmith layout, page design |
| Hamad (lead) | Leaflet map integration |
| Maria (lead) | Logic and states, interactivity |

## Appsmith Export

When you make changes in Appsmith, export the app JSON and commit it here in `frontend/appsmith/`.
This way we can track changes in git.
