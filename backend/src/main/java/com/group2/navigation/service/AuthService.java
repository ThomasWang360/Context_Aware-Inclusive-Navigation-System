package com.group2.navigation.service;

import com.group2.navigation.model.User;
import com.group2.navigation.model.UserPreferences;
import com.group2.navigation.repository.MessageRepository;
import com.group2.navigation.repository.SavedRouteRepository;
import com.group2.navigation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handles user signup and login.
 * Passwords are hashed with BCrypt before storing in H2.
 */
@Service
public class AuthService {

    private static final Pattern EMAIL_FORMAT = Pattern.compile(
            "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private SavedRouteRepository savedRouteRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Register a new user.
     *
     * @throws IllegalArgumentException if the username is already taken
     */
    public User signup(String username, String password, String displayName, String location) {
        if (userRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User(username, encoder.encode(password), displayName);
        if (location != null && !location.isBlank()) {
            user.setLocation(location.trim());
        }
        return userRepo.save(user);
    }

    /**
     * Authenticate an existing user.
     *
     * @return the User if credentials are valid
     * @throws IllegalArgumentException if username not found or password is wrong
     */
    public User login(String username, String password) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return user;
    }

    /** Update a user's saved route preferences. */
    public User updatePreferences(Long userId, UserPreferences prefs) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.applyPreferences(prefs);
        return userRepo.save(user);
    }

    /** Get a user by ID. */
    public Optional<User> getUser(Long userId) {
        return userRepo.findById(userId);
    }

    /**
     * Update email and/or password. At least one non-blank value must be provided.
     */
    @Transactional
    public User updateCredentials(Long userId, String emailRaw, String passwordRaw) {
        String email = normalizeOptional(emailRaw);
        String password = passwordRaw == null || passwordRaw.isBlank() ? null : passwordRaw;

        if (email == null && password == null) {
            throw new IllegalArgumentException("At least one of email or password must be provided");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (email != null) {
            if (!EMAIL_FORMAT.matcher(email).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
            Optional<User> existing = userRepo.findByEmailIgnoreCase(email);
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(email);
        }

        if (password != null) {
            user.setPassword(encoder.encode(password));
        }

        return userRepo.save(user);
    }

    /**
     * Delete the user and all messages where they are sender or receiver.
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        messageRepo.deleteAllInvolvingUser(userId);
        savedRouteRepo.deleteAllByUserId(userId);
        userRepo.deleteById(userId);
    }

    private static String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        return t.isEmpty() ? null : t;
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String query, Long excludeUserId) {
    List<User> users;

    if (query == null || query.isBlank()) {
        users = userRepo.findAllByOrderByDisplayNameAsc();
    } else {
        String q = query.trim();
        users = userRepo
                .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByDisplayNameAsc(q, q);
    }

    return users.stream()
            .filter(user -> excludeUserId == null || !user.getId().equals(excludeUserId))
            .toList();
}

}
