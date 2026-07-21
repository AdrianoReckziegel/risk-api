package com.adriano.risk_api.config;

import com.adriano.risk_api.entity.Role;
import com.adriano.risk_api.entity.User;
import com.adriano.risk_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeUsers() {

        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User user = new User();

                user.setUsername("admin");
                user.setPassword(
                        passwordEncoder.encode("admin123")
                );
                user.setRole(Role.ADMIN);

                userRepository.save(user);
            }
        };
    }
}