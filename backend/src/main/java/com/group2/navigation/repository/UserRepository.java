package com.group2.navigation.repository;

import com.group2.navigation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for user account lookups.
 * Username is unique, so findByUsername is the main query used by login.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByOrderByDisplayNameAsc();

    List<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByDisplayNameAsc(
        String username,
        String displayName
);
}
