package com.lucabridge.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point. Replaces the old BlogApplication: the site is no longer a blog with extras,
 * it is a content platform of which the blog is one type.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LucaBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LucaBridgeApplication.class, args);
    }
}
