package com.server.back.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		//todo
		registry.addMapping("/**")
			.allowedOrigins("http://localhost:8080", "http://localhost:3000", "https://jungle-school.xyz", "https://modoostock.com", "http://localhost:5173", "https://jungle-school.store")
			.allowedMethods("GET", "POST", "PUT", "DELETE")
			.exposedHeaders("Access-Control-Allow-Headers, Authorization, X-Refresh-Token")
			.allowCredentials(true);
	}

}

