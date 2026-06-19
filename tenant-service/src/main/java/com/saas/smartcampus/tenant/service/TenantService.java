package com.saas.smartcampus.tenant.service;

import com.saas.smartcampus.tenant.entity.Tenant;
import com.saas.smartcampus.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant onboardTenant(Tenant tenant) {
        log.info("Onboarding new tenant: name={}, subdomain={}", tenant.getName(), tenant.getSubdomain());
        if (tenantRepository.findBySubdomain(tenant.getSubdomain()).isPresent()) {
            throw new IllegalArgumentException("Subdomain '" + tenant.getSubdomain() + "' is already registered!");
        }
        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with ID " + id + " not found!"));
    }

    public Tenant getTenantBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new IllegalArgumentException("Tenant with subdomain '" + subdomain + "' not found!"));
    }

    @Transactional
    public Tenant updateTenant(Long id, Tenant tenantDetails) {
        Tenant tenant = getTenantById(id);
        tenant.setName(tenantDetails.getName());
        tenant.setPlan(tenantDetails.getPlan());
        tenant.setStatus(tenantDetails.getStatus());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = getTenantById(id);
        tenantRepository.delete(tenant);
    }
}
