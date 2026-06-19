package com.saas.smartcampus.auth.dto;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String tenantId;

    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String password, String role, String tenantId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.tenantId = tenantId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public static RegisterRequestBuilder builder() {
        return new RegisterRequestBuilder();
    }

    public static class RegisterRequestBuilder {
        private String name;
        private String email;
        private String password;
        private String role;
        private String tenantId;

        public RegisterRequestBuilder name(String name) { this.name = name; return this; }
        public RegisterRequestBuilder email(String email) { this.email = email; return this; }
        public RegisterRequestBuilder password(String password) { this.password = password; return this; }
        public RegisterRequestBuilder role(String role) { this.role = role; return this; }
        public RegisterRequestBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public RegisterRequest build() {
            return new RegisterRequest(name, email, password, role, tenantId);
        }
    }
}
