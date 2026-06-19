package com.saas.smartcampus.shared.config;

import com.saas.smartcampus.shared.filter.TenantFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@AutoConfiguration
@ComponentScan(basePackages = {
    "com.saas.smartcampus.shared.aspect",
    "com.saas.smartcampus.shared.filter",
    "com.saas.smartcampus.shared.security"
})
@EnableAspectJAutoProxy
@EnableTransactionManagement(order = 50) // Runs transaction interceptor before our AOP filter aspect
public class TenantAutoConfiguration {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantFilter filter) {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setName("tenantFilter");
        registration.setOrder(1); // Ensure it runs early in the filter chain
        return registration;
    }
}
