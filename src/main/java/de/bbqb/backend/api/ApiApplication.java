package de.bbqb.backend.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.firestore.repository.config.EnableReactiveFirestoreRepositories;

/**
 * Configure and start the application
 * 
 * @author laster
 *
 */
@SpringBootApplication(scanBasePackages = { "de.bbqb" })
@EnableReactiveFirestoreRepositories("de.bbqb.backend.gcp.firestore")
public class ApiApplication {

	/**
	 * Start the application as a Spring application and pass cmd arguments
	 * 
	 * @param args Arguments passed to the application on startup
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
