# Context-Aware Inclusive Navigation System

We are building a navigation app for the GTA that helps you find routes based on accessibility, safety, and personal preferences.

## What This Does

Most navigation apps just give you the shortest route. But what if you're in a wheelchair and need to avoid stairs? Or walking home late at night and want well-lit streets? Or you need to stay close to hospitals?

This app considers:
- **Wheelchair accessibility** - curb cuts, ramps, surface quality
- **Safety** - crime rates, street lighting (especially for night routes)
- **Construction zones** - avoid noisy/blocked areas
- **Nearby services** - hospitals, convenience stores, etc.

We're building this for the Greater Toronto Area using open data from the city.

## Team

| Name | Role | Email |
|------|------|-------|
| Arshia Feizmohammady | Backend Lead, Algorithm Design | arshia.feizmohammady@mail.utoronto.ca |
| Thomas (Jigao) Wang | Backend & Integration Lead | jigao.wang@mail.utoronto.ca |
| Andrew Yin | Backend, Algorithm Design | realandrew.yin@mail.utoronto.ca |
| Simon Zhu | Backend, Algorithm | simon.zhu@mail.utoronto.ca |
| Hamad Almheiri | Frontend Lead | h.almheiri@mail.utoronto.ca |
| Dooyeon (Dunny) Yeom | Frontend, Project Manager | dooyeon.yeom@mail.utoronto.ca |
| Ivan Musuba | Frontend | ivan.musuba@mail.utoronto.ca |
| Maria Chzhen | Frontend, Algorithm Design | maria.chzhen@mail.utoronto.ca |

## Tech Stack

- **Backend**: Java + Spring Boot
- **Database**: H2 (embedded)
- **Frontend**: Appsmith (self-hosted) + Leaflet.js for maps
- **Routing Algorithm**: A* with custom edge weights
- **Map Data**: OpenStreetMap
- **Deployment**: Docker

## Repo Structure

```
├── backend/          # Java Spring Boot app
├── frontend/         # Appsmith stuff
├── data/             # Raw data + processing scripts
└── docs/             # ER diagrams, API docs, etc.
```

## Who's Working on What

| Task | Lead | Also Working On It | Where in Repo |
|------|------|--------------------|---------------|
| Data Sourcing | Arshia | Andrew, Simon, Maria | `data/` |
| Graph Setup | Andrew | Arshia, Simon, Maria | `backend/.../algorithm/` |
| A* Algorithm | Maria | Andrew, Arshia, Simon | `backend/.../algorithm/AStarRouter.java` |
| Data Modeling (ER, UML) | Arshia | Thomas | `docs/`, `backend/.../model/` |
| Database Implementation | Thomas | Arshia, Andrew | `backend/.../repository/` |
| REST API | Simon | Thomas | `backend/.../controller/`, `backend/.../service/` |
| Appsmith Layout | Dooyeon | Ivan | `frontend/appsmith/` |
| Map Integration | Hamad | Ivan | `frontend/appsmith/` |
| Logic and States | Maria | Dooyeon | `frontend/appsmith/` |
| API Integration | Thomas | Everyone | - |
| Testing + Docker | Everyone | - | `docker-compose.yml` |

## How to Run

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
The API will be at `http://localhost:8080`

### Frontend (Appsmith)
```bash
docker-compose up appsmith
```
Then go to `http://localhost:80`

### Everything at Once
```bash
docker-compose up
```

## Data Sources

We're using open data from:
- [Toronto Open Data](https://open.toronto.ca/) - crime stats, street lights, construction
- [Toronto Police Open Data](https://data.torontopolice.on.ca/) - crime data
- [OpenStreetMap](https://www.openstreetmap.org/) - base map, POIs, accessibility info

OSM data for Ontario: [Geofabrik Download](https://download.geofabrik.de/north-america/canada/ontario.html)


## API Endpoints

| Method | Endpoint | What it does |
|--------|----------|--------------|
| POST | `/api/route` | Calculate a route with preferences |
| GET | `/api/preferences` | Get default preference values |
| GET | `/api/pois` | Get nearby points of interest |

More details in `docs/API_Documentation.md` (once we write it).

---

MIE350 - Group 2
