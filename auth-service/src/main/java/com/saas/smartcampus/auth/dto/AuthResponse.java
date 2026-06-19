package com.saas.smartcampus.auth.dto;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String role;
    private String tenantId;

    public AuthResponse() {}

    public AuthResponse(String token, String name, String email, String role, String tenantId) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private String name;
        private String email;
        private String role;
        private String tenantId;

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder name(String name) { this.name = name; return this; }
        public AuthResponseBuilder email(String email) { this.email = email; return this; }
        public AuthResponseBuilder role(String role) { this.role = role; return this; }
        public AuthResponseBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, name, email, role, tenantId);
        }
    }
}
