package com.abcgreen.evnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class EvChargeNetApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(EvChargeNetApplication.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder b) {
        return b.sources(EvChargeNetApplication.class);
    }
}
