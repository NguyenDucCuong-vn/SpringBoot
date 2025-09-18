package com.example.user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.service.JwtService;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.model.User;
import io.jsonwebtoken.Claims;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.CacheEvict;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String tokenAccess = jwtService.generateTokenAccess(user);
        String tokenRefresh = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(Map.of("tokenAccess", tokenAccess,"tokenRefresh", tokenRefresh));
    }

    @PostMapping("/register")
    @CacheEvict(value = "allUsers", allEntries = true)
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String name = body.get("name");
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }
        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .build();
        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> takeAccessToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");       
        Claims claims = jwtService.extractClaims(refreshToken);
        
        // Kiểm tra token type phải là "refresh"
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
        }
        
        Long userId = claims.get("userId", Long.class);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        String accessToken = jwtService.generateTokenAccess(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", newRefreshToken));
    }
    
}
