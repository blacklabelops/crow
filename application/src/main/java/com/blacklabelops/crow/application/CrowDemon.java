package com.blacklabelops.crow.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.blacklabelops.crow.config.Crow;

/**
 * Created by steffenbleul on 28.12.16.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "com.blacklabelops.crow" })
@EnableConfigurationProperties(Crow.class)
public class CrowDemon {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CrowDemon.class);
        app.run(args);
    }
}
