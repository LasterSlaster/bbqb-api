package de.bbqb.backend.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // URL to stripe server returning its webhook IPs
    @Value("${bbq.backend.stripe.webhook-ips-url}")
    String stripeWebhookIPsURL;

    /**
     * To receive Webhook requests from Stripe we need to whitelist their servers ip addresses
     * @return A List with accepted ip addresses
     */
    @Bean
    public List<String> validStripeIps() {
        List<String> validStripeIps = new ArrayList<>();
        FileInputStream inputStream = null;
        try {
            Scanner scanner = new Scanner(new URL(stripeWebhookIPsURL).openStream());
            validStripeIps.add(scanner.nextLine());
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
