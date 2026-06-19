package com.saas.smartcampus.fee;

import com.saas.smartcampus.fee.client.AuthClient;
import com.saas.smartcampus.fee.dto.UserDto;
import com.saas.smartcampus.shared.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.openfeign.client.config.auth-service.url=http://localhost:${wiremock.server.port}",
        "eureka.client.enabled=false"
})
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public class FeeServiceIntegrationTest {

    @Autowired
    private AuthClient authClient;

    @AfterEach
    public void tearDown() {
        TenantContext.clear();
    }

    @Test
    public void testFeignClientPropagatesTenantHeader() {
        // Mock downstream auth-service response
        stubFor(get(urlEqualTo("/api/auth/users/777"))
                .withHeader("X-Tenant-ID", equalTo("mit"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\":777,\"name\":\"John Doe\",\"email\":\"john@doe.com\",\"role\":\"STUDENT\"}")
                        .withStatus(200)));

        // Set the active tenant context
        TenantContext.setCurrentTenant("mit");

        // Invoke Feign client
        UserDto user = authClient.getUserById(777L);

        // Verify Feign client correctly mapped response and propagated the X-Tenant-ID header
        assertNotNull(user);
        assertEquals(777L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("STUDENT", user.getRole());
    }
}
