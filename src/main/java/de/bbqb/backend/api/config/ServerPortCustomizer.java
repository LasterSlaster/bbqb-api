package de.bbqb.backend.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Configure Webserver Port by reading port information from properties file otherwise use default port 8080.
 * 
 * @author laster
 *
 */
@Component
public class ServerPortCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Autowired
    private Environment env;		  
    
    @Value("bbq.backend.webserver.port-env-variable")
    private String WebserverPortEnvVariable;
    
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerPortCustomizer.class);

	@Override
    public void customize(ConfigurableWebServerFactory factory) {
		String portValue = env.getProperty(WebserverPortEnvVariable);
		int port = 8080;
		try {
			port = Integer.parseInt(portValue);
			LOGGER.info("Configure webserver port with port number " + String.valueOf(port) + " specified by environment variable " + WebserverPortEnvVariable);
		} catch (NumberFormatException e) {
			LOGGER.info("Unable to resolve a valid port number from environment variable " + WebserverPortEnvVariable + ". Fallback to default port 8080.");
		}
		
        factory.setPort(port);
    }
}
