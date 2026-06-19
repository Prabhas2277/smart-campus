package com.saas.smartcampus.gateway.filter;

import com.saas.smartcampus.gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        // Configuration fields if needed
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.info("Processing gateway request path: {}", path);

            // Bypass authentication for public paths
            if (isPublicPath(path)) {
                log.info("Bypassing JWT validation for public path: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("Token is expired for path: {}", path);
                    return onError(exchange, "Token is expired", HttpStatus.UNAUTHORIZED);
                }

                String tenantId = jwtUtil.extractTenantId(token);
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                if (tenantId == null || tenantId.trim().isEmpty()) {
                    log.warn("Tenant ID claim missing in token for path: {}", path);
                    return onError(exchange, "Tenant ID claim missing in token", HttpStatus.UNAUTHORIZED);
                }

                log.info("Request authenticated successfully. Tenant: {}, User: {}, Role: {}", tenantId, username, role);

                // Mutate the request to inject X-Tenant-ID, X-Auth-User, and X-User-Role
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Tenant-ID", tenantId)
                        .header("X-Auth-User", username)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.error("Token verification failed for path: {}, error: {}", path, e.getMessage());
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/register") ||
               path.startsWith("/api/tenants");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
