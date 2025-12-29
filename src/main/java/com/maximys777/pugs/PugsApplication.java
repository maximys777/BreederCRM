package com.maximys777.pugs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PugsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PugsApplication.class, args);
    }

}
