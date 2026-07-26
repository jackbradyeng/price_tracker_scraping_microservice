package org.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScrapingMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapingMicroserviceApplication.class, args);
    }
}