package com.jobportal.auth.service;

import com.jobportal.auth.dto.*;
import com.jobportal.auth.entity.User;
import com.jobportal.auth.exception.AuthException;
import com.jobportal.auth.repository.UserRepository;
import com.jobportal.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AuthException("Unable to register with these details", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .phone(request.phone())
                .build();

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);

        User saved = userRepository.save(user);

        return new AuthResponse(UserResponse.from(saved), accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(UserResponse.from(user), accessToken, refreshToken);
    }

    @Transactional
    public String refreshAccessToken(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtUtil.isRefreshTokenValid(token)) {
            throw new AuthException("Invalid or expired refresh token", HttpStatus.FORBIDDEN);
        }

        String email = jwtUtil.extractEmailFromRefreshToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.FORBIDDEN));

        // Confirms the token matches what's stored and allows revoking all
        if (!token.equals(user.getRefreshToken())) {
            throw new AuthException("Refresh token not recognized", HttpStatus.FORBIDDEN);
        }

        return jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.NOT_FOUND));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.NOT_FOUND));
        return UserResponse.from(user);
    }
}
