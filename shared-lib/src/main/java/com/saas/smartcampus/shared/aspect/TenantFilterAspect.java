package com.saas.smartcampus.shared.aspect;

import com.saas.smartcampus.shared.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(100) // Runs after @EnableTransactionManagement (order 50)
public class TenantFilterAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantFilterAspect.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void enableTenantFilter() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("TenantFilterAspect: Current tenant is {}", tenantId);
        System.out.println("TenantFilterAspect: Current tenant is " + tenantId);
        if (tenantId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                if (session != null && session.isOpen()) {
                    Filter filter = session.enableFilter("tenantFilter");
                    if (filter != null) {
                        filter.setParameter("tenantId", tenantId);
                        log.debug("TenantFilterAspect: Enabled filter 'tenantFilter' with tenantId={}", tenantId);
                        System.out.println("TenantFilterAspect: Enabled filter 'tenantFilter' with tenantId=" + tenantId);
                    }
                }
            } catch (Exception e) {
                log.warn("TenantFilterAspect: Failed to enable filter", e);
                System.out.println("TenantFilterAspect: Failed to enable filter: " + e.getMessage());
            }
        }
    }
}
