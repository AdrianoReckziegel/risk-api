package com.adriano.risk_api.config;

import com.adriano.risk_api.entity.Role;
import com.adriano.risk_api.entity.User;
import com.adriano.risk_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap-admin.username:admin}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap-admin.password:admin123}")
    private String bootstrapAdminPassword;

    @Bean
    public CommandLineRunner initializeUsers() {

        return args -> {

            if (!bootstrapAdminEnabled) {
                return;
            }

            if (userRepository.findByUsername(bootstrapAdminUsername).isEmpty()) {

                User user = new User();

                user.setUsername(bootstrapAdminUsername);
                user.setPassword(
                        passwordEncoder.encode(bootstrapAdminPassword)
                );
                user.setRole(Role.ADMIN);

                userRepository.save(user);
            }
        };
    }
}