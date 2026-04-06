package com.group2.navigation.repository;

import com.group2.navigation.model.SavedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedRouteRepository extends JpaRepository<SavedRoute, Long> {

    List<SavedRoute> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
