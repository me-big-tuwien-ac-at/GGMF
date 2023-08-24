package com.modcmga.backendservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableCaching
@EntityScan("com.modcmga.backendservice.entity")
@SpringBootApplication
public class BackendserviceApplication {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/apply").allowedOrigins("http://localhost:4200");
				registry.addMapping("/modularise").allowedOrigins("http://localhost:4200");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendserviceApplication.class, args);
	}

}
