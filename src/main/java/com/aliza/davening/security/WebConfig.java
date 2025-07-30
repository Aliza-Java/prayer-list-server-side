package com.aliza.davening.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("https://localhost:4200",
				"http://localhost:4200",
                "https://www.emekhafrashatchallah.com", 
                "https://emekhafrashatchallah.com") // Allow Angular app in all envs
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowCredentials(true)
                 .allowedHeaders("*")
                 .exposedHeaders("Token-Expired") 
                 .allowCredentials(true);
				;
	}
}