package com.saas.smartcampus.shared.entity;

import com.saas.smartcampus.shared.context.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
public abstract class AbstractTenantEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            String current = TenantContext.getCurrentTenant();
            if (current == null) {
                throw new IllegalStateException("Cannot persist tenant-scoped entity: Tenant ID is not set in TenantContext!");
            }
            this.tenantId = current;
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
