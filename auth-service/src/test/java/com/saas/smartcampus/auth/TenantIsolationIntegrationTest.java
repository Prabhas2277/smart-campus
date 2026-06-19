package com.saas.smartcampus.auth;

import com.saas.smartcampus.auth.dto.AuthResponse;
import com.saas.smartcampus.auth.dto.RegisterRequest;
import com.saas.smartcampus.auth.entity.User;
import com.saas.smartcampus.auth.repository.UserRepository;
import com.saas.smartcampus.auth.service.UserService;
import com.saas.smartcampus.shared.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TenantIsolationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void cleanup() {
        TenantContext.setCurrentTenant("tenant-a");
        userRepository.deleteAll();
        TenantContext.setCurrentTenant("tenant-b");
        userRepository.deleteAll();
        TenantContext.clear();
    }

    @Test
    public void testTenantIsolationAndOverlappingData() {
        // 1. Register Alice in Tenant A
        RegisterRequest requestA = RegisterRequest.builder()
                .name("Alice Tenant A")
                .email("alice@campus.com")
                .password("password123")
                .role("Student")
                .tenantId("tenant-a")
                .build();

        AuthResponse responseA = userService.register(requestA);
        assertNotNull(responseA);
        assertEquals("tenant-a", responseA.getTenantId());

        // 2. Register Alice in Tenant B (overlapping email, same email!)
        RegisterRequest requestB = RegisterRequest.builder()
                .name("Alice Tenant B")
                .email("alice@campus.com")
                .password("password456")
                .role("Student")
                .tenantId("tenant-b")
                .build();

        AuthResponse responseB = userService.register(requestB);
        assertNotNull(responseB);
        assertEquals("tenant-b", responseB.getTenantId());

        // 3. Set TenantContext to Tenant A and verify queries
        TenantContext.setCurrentTenant("tenant-a");
        
        List<User> usersA = userRepository.findAll();
        assertEquals(1, usersA.size());
        assertEquals("Alice Tenant A", usersA.get(0).getName());
        assertEquals("tenant-a", usersA.get(0).getTenantId());

        Optional<User> userAOptional = userRepository.findByEmail("alice@campus.com");
        assertTrue(userAOptional.isPresent());
        assertEquals("Alice Tenant A", userAOptional.get().getName());

        // 4. Set TenantContext to Tenant B and verify queries
        TenantContext.setCurrentTenant("tenant-b");

        List<User> usersB = userRepository.findAll();
        assertEquals(1, usersB.size());
        assertEquals("Alice Tenant B", usersB.get(0).getName());
        assertEquals("tenant-b", usersB.get(0).getTenantId());

        Optional<User> userBOptional = userRepository.findByEmail("alice@campus.com");
        assertTrue(userBOptional.isPresent());
        assertEquals("Alice Tenant B", userBOptional.get().getName());

        // 5. Test saving without setting tenantId explicitly on Entity (prePersist checks)
        TenantContext.setCurrentTenant("tenant-a");
        User userWithoutTenantId = User.builder()
                .name("Bob Tenant A")
                .email("bob@campus.com")
                .password("bobpwd")
                .role("Faculty")
                .build();

        User savedBob = userRepository.save(userWithoutTenantId);
        assertEquals("tenant-a", savedBob.getTenantId()); // Auto-populated!
    }
}
