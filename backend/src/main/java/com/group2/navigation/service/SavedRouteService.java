package com.group2.navigation.service;

import com.group2.navigation.dto.SavedRouteRequest;
import com.group2.navigation.model.SavedRoute;
import com.group2.navigation.model.User;
import com.group2.navigation.repository.SavedRouteRepository;
import com.group2.navigation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SavedRouteService {

    private static final int MAX_ROUTES_PER_USER = 20;

    @Autowired
    private SavedRouteRepository routeRepo;

    @Autowired
    private UserRepository userRepo;

    @Transactional
    public Map<String, Object> create(SavedRouteRequest req) {
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (routeRepo.countByUserId(req.getUserId()) >= MAX_ROUTES_PER_USER) {
            throw new IllegalArgumentException("Maximum " + MAX_ROUTES_PER_USER + " saved routes per user");
        }

        SavedRoute route = new SavedRoute();
        route.setUser(user);
        applyFields(route, req);

        return toMap(routeRepo.save(route));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findByUser(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        return routeRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(SavedRouteService::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> update(Long routeId, SavedRouteRequest req) {
        SavedRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Saved route not found"));

        if (!route.getUser().getId().equals(req.getUserId())) {
            throw new IllegalArgumentException("Route does not belong to this user");
        }

        applyFields(route, req);
        return toMap(routeRepo.save(route));
    }

    @Transactional
    public void delete(Long routeId, Long userId) {
        SavedRoute route = routeRepo.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Saved route not found"));

        if (!route.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Route does not belong to this user");
        }

        routeRepo.deleteById(routeId);
    }

    private void applyFields(SavedRoute route, SavedRouteRequest req) {
        route.setName(req.getName().trim());
        route.setStartAddress(req.getStartAddress() != null ? req.getStartAddress().trim() : null);
        route.setEndAddress(req.getEndAddress() != null ? req.getEndAddress().trim() : null);
        route.setStartLat(req.getStartLat());
        route.setStartLng(req.getStartLng());
        route.setEndLat(req.getEndLat());
        route.setEndLng(req.getEndLng());
        route.setWheelchairWeight(req.getWheelchairWeight());
        route.setCrimeWeight(req.getCrimeWeight());
        route.setLightingWeight(req.getLightingWeight());
        route.setConstructionWeight(req.getConstructionWeight());
        route.setTimeOfDay(req.getTimeOfDay());
        route.setMaxDistanceToHospital(req.getMaxDistanceToHospital());
    }

    public static Map<String, Object> toMap(SavedRoute r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("userId", r.getUser().getId());
        m.put("name", r.getName());
        m.put("startAddress", r.getStartAddress());
        m.put("endAddress", r.getEndAddress());
        m.put("startLat", r.getStartLat());
        m.put("startLng", r.getStartLng());
        m.put("endLat", r.getEndLat());
        m.put("endLng", r.getEndLng());
        m.put("wheelchairWeight", r.getWheelchairWeight());
        m.put("crimeWeight", r.getCrimeWeight());
        m.put("lightingWeight", r.getLightingWeight());
        m.put("constructionWeight", r.getConstructionWeight());
        m.put("timeOfDay", r.getTimeOfDay());
        m.put("maxDistanceToHospital", r.getMaxDistanceToHospital());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }
}
