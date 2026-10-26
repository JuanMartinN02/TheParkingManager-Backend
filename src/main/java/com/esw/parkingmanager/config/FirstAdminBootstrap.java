package com.esw.parkingmanager.config;

import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FirstAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FirstAdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public FirstAdminBootstrap(AppUserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.first-admin.email}") String email,
                               @Value("${app.first-admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Only the very first start creates an admin. After that this does nothing
        if (userRepository.existsByRole(Role.SUPER_ADMIN)) {
            return;
        }

        if (email.isBlank() || password.isBlank()) {
            log.warn("No SUPER_ADMIN exists and FIRST_ADMIN_EMAIL / FIRST_ADMIN_PASSWORD are not set. Nobody can log in");
            return;
        }

        // Stop the app rather than create the master account with a weak password
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("FIRST_ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        AppUser admin = new AppUser();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.SUPER_ADMIN);
        // The owner picked this password, no need to force a change
        admin.setMustChangePassword(false);
        userRepository.save(admin);

        // Never log the password, not even in dev
        log.info("Created first SUPER_ADMIN {}", admin.getEmail());
    }
}