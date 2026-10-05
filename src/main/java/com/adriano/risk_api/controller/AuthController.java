package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.AccessRequest;
import com.adriano.risk_api.dto.AccessRequestResponse;
import com.adriano.risk_api.dto.LoginRequest;
import com.adriano.risk_api.dto.LoginResponse;
import com.adriano.risk_api.security.JwtService;
import com.adriano.risk_api.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "Authentication", description = "Authentication and access request endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Operation(summary = "User login", description = "Authenticates user and returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        String token =
                jwtService.generateToken(
                        (UserDetails) authentication.getPrincipal()
                );

        return ResponseEntity.ok(new LoginResponse(token));

    }

    @Operation(summary = "Request portal access", description = "Submits an access request and notifies administrator via email")
    @PostMapping("/request-access")
    public ResponseEntity<AccessRequestResponse> requestAccess(
            @Valid @RequestBody AccessRequest request) {

        emailService.sendAccessRequestNotification(request);

        return ResponseEntity.ok(AccessRequestResponse.builder()
                .message("Access request submitted successfully.")
                .timestamp(Instant.now())
                .build());
    }

}