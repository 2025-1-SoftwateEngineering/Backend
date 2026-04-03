package com.example.vocabook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VocaBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocaBookApplication.class, args);
    }

}
