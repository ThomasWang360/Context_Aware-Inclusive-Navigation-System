package com.group2.navigation.data;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Downloads and caches OpenStreetMap road network data from the Overpass API.
 *
 * On first run, this fetches all walkable/cycleable roads in the configured
 * bounding box and saves the JSON response to a local file. Subsequent runs
 * use the cached file, so the download only happens once.
 */
public class OsmDataLoader {

    private static final String OVERPASS_API = "https://overpass-api.de/api/interpreter";

    // Downtown Toronto bounding box: south-lat, west-lng, north-lat, east-lng
    private static final String DEFAULT_BBOX = "43.625,-79.425,43.705,-79.340";

    private static final String DEFAULT_CACHE_PATH = "data/osm_toronto.json";

    private static final String HIGHWAY_TYPES =
            "footway|cycleway|path|pedestrian|residential|tertiary|tertiary_link|"
            + "secondary|secondary_link|primary|primary_link|living_street|"
            + "unclassified|steps|trunk|trunk_link";

    /**
     * Ensure OSM data is available locally, downloading it if necessary.
     *
     * @return path to the cached JSON file, or null if download failed
     */
    public static String ensureOsmData() {
        return ensureOsmData(DEFAULT_CACHE_PATH, DEFAULT_BBOX);
    }

    public static String ensureOsmData(String cachePath, String bbox) {
        Path cache = Path.of(cachePath);

        if (Files.exists(cache)) {
            try {
                if (Files.size(cache) > 1000) {
                    System.out.println("[OSM] Using cached data from " + cachePath);
                    return cachePath;
                }
            } catch (IOException ignored) {}
        }

        System.out.println("[OSM] No cached data found. Downloading from Overpass API...");
        System.out.println("[OSM] Bounding box: " + bbox);
        System.out.println("[OSM] This may take 1-2 minutes on first run.");

        try {
            downloadFromOverpass(bbox, cachePath);
            System.out.println("[OSM] Data downloaded and cached at " + cachePath);
            return cachePath;
        } catch (Exception e) {
            System.err.println("[OSM] Failed to download: " + e.getMessage());
            return null;
        }
    }

    private static void downloadFromOverpass(String bbox, String outputPath)
            throws IOException, InterruptedException {

        String query = String.format(
                "[out:json][timeout:90];"
                + "way[\"highway\"~\"^(%s)$\"](%s);"
                + "(._;>;);"
                + "out body;",
                HIGHWAY_TYPES, bbox);

        String body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OVERPASS_API))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(180))
                .build();

        Path path = Path.of(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Path tempFile = path.resolveSibling(path.getFileName() + ".tmp");

        HttpResponse<Path> response = client.send(
                request, HttpResponse.BodyHandlers.ofFile(tempFile));

        if (response.statusCode() != 200) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Overpass API returned HTTP " + response.statusCode());
        }

        long fileSize = Files.size(tempFile);
        if (fileSize < 100) {
            String content = Files.readString(tempFile);
            Files.deleteIfExists(tempFile);
            throw new IOException("Response too small (" + fileSize + " bytes): " + content);
        }

        Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[OSM] Downloaded " + (fileSize / 1024) + " KB");
    }
}
