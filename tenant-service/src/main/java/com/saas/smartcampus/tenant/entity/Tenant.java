package com.saas.smartcampus.tenant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", uniqueConstraints = {
    @UniqueConstraint(columnNames = "subdomain")
})
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String subdomain;

    @Column(nullable = false)
    private String plan; // e.g. FREE, PREMIUM, ENTERPRISE

    @Column(nullable = false)
    private String status; // e.g. ACTIVE, INACTIVE, SUSPENDED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    // Constructors
    public Tenant() {}

    public Tenant(Long id, String name, String subdomain, String plan, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.subdomain = subdomain;
        this.plan = plan;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubdomain() { return subdomain; }
    public void setSubdomain(String subdomain) { this.subdomain = subdomain; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder Pattern
    public static TenantBuilder builder() {
        return new TenantBuilder();
    }

    public static class TenantBuilder {
        private Long id;
        private String name;
        private String subdomain;
        private String plan;
        private String status;
        private LocalDateTime createdAt;

        TenantBuilder() {}

        public TenantBuilder id(Long id) { this.id = id; return this; }
        public TenantBuilder name(String name) { this.name = name; return this; }
        public TenantBuilder subdomain(String subdomain) { this.subdomain = subdomain; return this; }
        public TenantBuilder plan(String plan) { this.plan = plan; return this; }
        public TenantBuilder status(String status) { this.status = status; return this; }
        public TenantBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Tenant build() {
            return new Tenant(id, name, subdomain, plan, status, createdAt);
        }
    }
}
