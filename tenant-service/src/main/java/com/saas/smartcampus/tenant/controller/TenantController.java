package com.saas.smartcampus.tenant.controller;

import com.saas.smartcampus.tenant.entity.Tenant;
import com.saas.smartcampus.tenant.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> onboardTenant(@RequestBody Tenant tenant) {
        Tenant createdTenant = tenantService.onboardTenant(tenant);
        return new ResponseEntity<>(createdTenant, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<Tenant> getTenantBySubdomain(@PathVariable String subdomain) {
        return ResponseEntity.ok(tenantService.getTenantBySubdomain(subdomain));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @RequestBody Tenant tenantDetails) {
        return ResponseEntity.ok(tenantService.updateTenant(id, tenantDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
