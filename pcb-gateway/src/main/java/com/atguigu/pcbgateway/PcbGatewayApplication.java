package com.atguigu.pcbgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PcbGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PcbGatewayApplication.class, args);
    }
}
