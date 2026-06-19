package com.saas.smartcampus.fee.config;

import com.saas.smartcampus.shared.context.TenantContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            template.header("X-Tenant-ID", tenantId);
        }
    }
}
