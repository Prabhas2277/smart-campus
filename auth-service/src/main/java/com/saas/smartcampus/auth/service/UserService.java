package com.saas.smartcampus.auth.service;

import com.saas.smartcampus.auth.dto.AuthResponse;
import com.saas.smartcampus.auth.dto.LoginRequest;
import com.saas.smartcampus.auth.dto.RegisterRequest;
import com.saas.smartcampus.auth.entity.User;
import com.saas.smartcampus.auth.repository.UserRepository;
import com.saas.smartcampus.shared.context.TenantContext;
import com.saas.smartcampus.shared.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String reqTenantId = request.getTenantId();
        if (reqTenantId == null || reqTenantId.trim().isEmpty()) {
            reqTenantId = TenantContext.getCurrentTenant();
        }
        if (reqTenantId == null || reqTenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID is required for registration!");
        }

        // Set context if not already set (e.g. if bypass filters)
        TenantContext.setCurrentTenant(reqTenantId);

        log.info("Registering user: email={}, tenant={}", request.getEmail(), reqTenantId);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' is already registered in this tenant!");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        user.setTenantId(reqTenantId);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getTenantId(), savedUser.getRole());

        return AuthResponse.builder()
                .token(token)
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .tenantId(savedUser.getTenantId())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String reqTenantId = request.getTenantId();
        if (reqTenantId == null || reqTenantId.trim().isEmpty()) {
            reqTenantId = TenantContext.getCurrentTenant();
        }
        if (reqTenantId == null || reqTenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID is required for login!");
        }

        TenantContext.setCurrentTenant(reqTenantId);

        log.info("Authenticating user: email={}, tenant={}", request.getEmail(), reqTenantId);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password!");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getTenantId(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found!"));
    }
}
