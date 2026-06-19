package com.saas.smartcampus.gateway;

import com.saas.smartcampus.gateway.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.gateway.routes[0].id=tenant-service-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/tenants/**",

        "spring.cloud.gateway.routes[1].id=auth-service-route",
        "spring.cloud.gateway.routes[1].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/auth/**",

        "spring.cloud.gateway.routes[2].id=library-service-route",
        "spring.cloud.gateway.routes[2].uri=http://localhost:${wiremock.server.port}",
        "spring.cloud.gateway.routes[2].predicates[0]=Path=/api/library/**",

        "eureka.client.enabled=false"
})
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public class GatewayRoutingAndAuthTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String SECRET_STRING = "saas-smart-campus-super-secret-key-development-only-256bits";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    private String token;

    @BeforeEach
    public void setup() {
        token = Jwts.builder()
                .subject("john_doe")
                .claim("tenantId", "mit")
                .claim("role", "STUDENT")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SECRET_KEY)
                .compact();
    }

    @Test
    public void testPublicEndpointsBypassAuth() {
        // Mock downstream Auth Service register call
        stubFor(post(urlEqualTo("/api/auth/register"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"token\":\"mock-token\"}")
                        .withStatus(201)));

        // Expect public bypass to succeed (forward to mock downstream without token)
        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"email\":\"test@test.com\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.token").isEqualTo("mock-token");
    }

    @Test
    public void testProtectedEndpointsRequireToken() {
        // Try calling protected route library-service without token
        webTestClient.get()
                .uri("/api/library/books")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void testProtectedEndpointsWithValidToken() {
        // Mock downstream Library Service call
        stubFor(get(urlEqualTo("/api/library/books"))
                .withHeader("X-Tenant-ID", equalTo("mit"))
                .withHeader("X-Auth-User", equalTo("john_doe"))
                .withHeader("X-User-Role", equalTo("STUDENT"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")
                        .withStatus(200)));

        // Expect request with token to succeed and headers to be injected downstream
        webTestClient.get()
                .uri("/api/library/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }
}
