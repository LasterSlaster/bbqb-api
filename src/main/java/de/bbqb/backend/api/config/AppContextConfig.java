package de.bbqb.backend.api.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Spring Context Configuration Class to provide additional beans ect. to
 * ApiApplication class
 *
 * @author Marius Degen
 */
@Configuration
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

    @Value("${bbq.backend.stripe.webhook-ips-url}")
    String stripeWebhookIPsURL;

    @Bean
    public List<String> validStripeIps() {
        List<String> validStripeIps = new ArrayList<>();
        FileInputStream inputStream = null;
        try {
            Scanner scanner = new Scanner(new URL(stripeWebhookIPsURL).openStream());
            validStripeIps.add(scanner.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Add localhost
        validStripeIps.add("127.0.0.1");
        return validStripeIps;
    }
}
