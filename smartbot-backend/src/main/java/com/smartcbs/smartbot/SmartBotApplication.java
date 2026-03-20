package com.smartcbs.smartbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.smartcbs.smartbot.entity")
@EnableJpaRepositories(basePackages = "com.smartcbs.smartbot.repository")
@EnableAsync
@EnableScheduling
public class SmartBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartBotApplication.class, args);
        System.out.println("SmartBot Backend Server Started Successfully on http://localhost:8080");
    }
}