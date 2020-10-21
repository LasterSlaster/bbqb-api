package de.bbqb.backend.api.config;

/**
 * Spring Context Configuration Class to provide additional beans ect. to
 * ApiApplication class
 *
 * @author Marius Degen
 */
//@Configuration
public class AppContextConfig {

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
