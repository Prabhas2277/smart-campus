package com.saas.smartcampus.fee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EntityScan(basePackages = {
    "com.saas.smartcampus.fee.entity",
    "com.saas.smartcampus.shared.entity"
})
@EnableDiscoveryClient
@EnableFeignClients
public class FeeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeeServiceApplication.class, args);
    }
}
