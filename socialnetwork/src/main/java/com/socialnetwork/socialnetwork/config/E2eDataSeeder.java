package com.socialnetwork.socialnetwork.config;

import java.util.logging.Logger;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.socialnetwork.business.interfaces.repository.IUserRepository;
import com.socialnetwork.socialnetwork.business.interfaces.service.IPrivacySettingsService;
import com.socialnetwork.socialnetwork.business.interfaces.service.IProfileService;
import com.socialnetwork.socialnetwork.entity.User;
import com.socialnetwork.socialnetwork.enums.UserRole;

/**
 * Seeds deterministic users for browser E2E runs (profile {@code e2e}).
 * Always resets passwords so re-runs fix previously double-encoded accounts.
 * Credentials: {@code e2e/fixtures/testData.js}
 */
@Component
@Profile("e2e")
public class E2eDataSeeder implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(E2eDataSeeder.class.getName());

    public static final String E2E_PASSWORD_PLAIN = "E2eTest!123";

    private final IUserRepository userRepository;
    private final IProfileService profileService;
    private final IPrivacySettingsService privacySettingsService;
    private final PasswordEncoder passwordEncoder;

    public E2eDataSeeder(
            IUserRepository userRepository,
            IProfileService profileService,
            IPrivacySettingsService privacySettingsService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.privacySettingsService = privacySettingsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        upsertUser("e2estudent", "e2e.student@eleve.isep.fr", "E2E", "Student", UserRole.STUDENT);
        upsertUser("e2epeer", "e2e.peer@eleve.isep.fr", "E2E", "Peer", UserRole.STUDENT);
        upsertUser("e2eadmin", "e2e.admin@isep.fr", "E2E", "Admin", UserRole.ADMIN);
    }

    private void upsertUser(String username, String email, String firstName, String lastName, UserRole role) {
        boolean isNew = userRepository.findByEmail(email).isEmpty();

        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(E2E_PASSWORD_PLAIN));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if (user.getBio() == null || user.getBio().isBlank()) {
            user.setBio("E2E seeded account for automated browser tests.");
        }
        user.setIsVerified(true);
        user.setIsActive(true);
        user.setRole(role);

        User saved = userRepository.save(user);
        LOGGER.info(() -> (isNew ? "Created" : "Updated password for") + " E2E user: " + email);

        if (isNew) {
            profileService.create(saved);
            privacySettingsService.create(saved);
        }
    }
}
