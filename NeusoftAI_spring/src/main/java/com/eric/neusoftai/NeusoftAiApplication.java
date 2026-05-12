package com.eric.neusoftai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NeusoftAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeusoftAiApplication.class, args);
    }

}
