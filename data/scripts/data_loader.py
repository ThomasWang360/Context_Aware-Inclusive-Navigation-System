"""
Data processing scripts for the navigation system.

These scripts download and process data from various sources
and prepare them for loading into the H2 database.

TODO (Arshia - lead, Andrew, Simon, Maria):
- Implement each loader function
- Add error handling
- Add progress bars for large files
"""

import os
import csv
import json

# Paths
RAW_DIR = os.path.join(os.path.dirname(__file__), '..', 'raw')
PROCESSED_DIR = os.path.join(os.path.dirname(__file__), '..', 'processed')


def process_crime_data(input_file: str, output_file: str):
    """
    Process crime data from Toronto Police.
    
    Input: CSV with crime incidents
    Output: JSON with crime scores per grid cell
    
    TODO: Implement this
    """
    print(f"Processing crime data: {input_file}")
    
    # Steps:
    # 1. Read CSV
    # 2. Filter to relevant crime types
    # 3. Aggregate by geographic grid
    # 4. Calculate crime score (0-1) per cell
    # 5. Output as JSON
    
    # Example output format:
    # {
    #   "grid_size": 0.001,  # degrees (~100m)
    #   "cells": [
    #     {"lat": 43.65, "lng": -79.38, "score": 0.3},
    #     ...
    #   ]
    # }
    
    pass


def process_street_lights(input_file: str, output_file: str):
    """
    Process street light locations from Toronto Open Data.
    
    Input: CSV/GeoJSON with light pole locations
    Output: JSON with light coverage areas
    
    TODO: Implement this
    """
    print(f"Processing street lights: {input_file}")
    
    # Steps:
    # 1. Read input file
    # 2. Extract lat/lng for each light
    # 3. Create coverage radius (e.g., 30m per light)
    # 4. Output as JSON
    
    pass


def process_construction(input_file: str, output_file: str):
    """
    Process construction/road restriction data.
    
    Input: CSV with road closures
    Output: JSON with active construction zones
    
    TODO: Implement this
    """
    print(f"Processing construction data: {input_file}")
    
    # Steps:
    # 1. Read CSV
    # 2. Filter to active restrictions (check dates)
    # 3. Extract affected road segments
    # 4. Output as JSON
    
    pass


def process_osm_extract(pbf_file: str, output_file: str):
    """
    Extract road network from OSM PBF file.
    
    This creates the graph JSON that the backend loads.
    
    Requires: osmium or osm4j
    
    TODO: Implement this (complex - might do in Java instead)
    """
    print(f"Processing OSM data: {pbf_file}")
    
    # This is complex - might be easier to do in Java with osm4j
    # Python option: use osmium library
    
    # Steps:
    # 1. Filter to highway=footway, cycleway, path, pedestrian, etc.
    # 2. Extract nodes and ways
    # 3. Build graph structure
    # 4. Output as JSON
    
    pass


def main():
    """
    Run all data processing steps.
    """
    print("Starting data processing...")
    
    # TODO: Uncomment and update paths when data is downloaded
    
    # process_crime_data(
    #     os.path.join(RAW_DIR, 'crime_data.csv'),
    #     os.path.join(PROCESSED_DIR, 'crime_scores.json')
    # )
    
    # process_street_lights(
    #     os.path.join(RAW_DIR, 'street_lights.csv'),
    #     os.path.join(PROCESSED_DIR, 'street_lights.json')
    # )
    
    # process_construction(
    #     os.path.join(RAW_DIR, 'road_restrictions.csv'),
    #     os.path.join(PROCESSED_DIR, 'construction.json')
    # )
    
    print("Data processing complete!")


if __name__ == '__main__':
    main()
