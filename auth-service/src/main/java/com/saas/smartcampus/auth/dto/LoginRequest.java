package com.saas.smartcampus.auth.dto;

public class LoginRequest {
    private String email;
    private String password;
    private String tenantId;

    public LoginRequest() {}

    public LoginRequest(String email, String password, String tenantId) {
        this.email = email;
        this.password = password;
        this.tenantId = tenantId;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    public static class LoginRequestBuilder {
        private String email;
        private String password;
        private String tenantId;

        public LoginRequestBuilder email(String email) { this.email = email; return this; }
        public LoginRequestBuilder password(String password) { this.password = password; return this; }
        public LoginRequestBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public LoginRequest build() {
            return new LoginRequest(email, password, tenantId);
        }
    }
}
