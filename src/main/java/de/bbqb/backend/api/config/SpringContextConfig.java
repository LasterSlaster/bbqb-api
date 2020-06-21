package de.bbqb.backend.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Context Configuration Class to provide additional beans ect. to
 * ApiApplication class
 * 
 * @author laster
 *
 */
//@Configuration
public class SpringContextConfig {

// Currenty CORS is configured with an annotation on the controller class
//	@Bean
//	public WebMvcConfigurer corsConfigurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/devices").allowedOrigins("*");
//			}
//		};
//	}
}
