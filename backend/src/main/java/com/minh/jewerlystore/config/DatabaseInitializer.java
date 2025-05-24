package com.minh.jewerlystore.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.minh.jewerlystore.entity.Role;
import com.minh.jewerlystore.entity.User;
import com.minh.jewerlystore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Initializing admin account...");
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@example.com");
            adminUser.setRole(Role.ROLE_ADMIN);
            
            userRepository.save(adminUser);
            
            log.info("Admin account created successfully");
        } else {
            log.info("Admin account already exists, skipping initialization");
        }
    }
} 