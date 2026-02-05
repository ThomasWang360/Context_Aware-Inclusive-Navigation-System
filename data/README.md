# Data

This folder contains raw data files and processing scripts.

## Folder Structure

```
data/
├── raw/          # Original data files (don't commit large files)
├── processed/    # Cleaned/processed data ready for the app
└── scripts/      # Python scripts for data processing
```

## Data Sources

### OpenStreetMap (Base Map + Graph)
- Download: https://download.geofabrik.de/north-america/canada/ontario.html
- Get the `.osm.pbf` file for Ontario
- We'll filter it down to GTA only

Bounding box for GTA (roughly):
- North: 44.0
- South: 43.4
- East: -78.8
- West: -80.0

### Toronto Open Data

| Data | URL | Format |
|------|-----|--------|
| Street Lights | https://open.toronto.ca/dataset/street-lighting/ | CSV/GeoJSON |
| Road Restrictions | https://open.toronto.ca/dataset/road-restrictions/ | CSV |
| Bike Lanes | https://open.toronto.ca/dataset/bikeways/ | GeoJSON |

### Toronto Police Open Data
- Crime data: https://data.torontopolice.on.ca/
- Look for "Major Crime Indicators" dataset

## Processing Steps

1. **Download raw data** → put in `raw/`
2. **Run processing scripts** → outputs go to `processed/`
3. **Load into H2 database** → done by backend at startup

## Who's Working on This

| Person | Task |
|--------|------|
| Arshia (lead) | Data sourcing, coordination |
| Andrew | OSM parsing |
| Simon | Data cleaning |
| Maria | Algorithm integration |

## Notes

- Don't commit large files (>100MB) to git
- Add data files to `.gitignore` if too big
- Document where you got each file
