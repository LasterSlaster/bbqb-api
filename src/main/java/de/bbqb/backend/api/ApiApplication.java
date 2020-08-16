package de.bbqb.backend.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.firestore.repository.config.EnableReactiveFirestoreRepositories;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;

/**
 * Configure and start the application
 * @author laster
 *
 */
@SpringBootApplication(scanBasePackages = { "de.bbqb" })
@EnableReactiveFirestoreRepositories("de.bbqb.backend.gcp.firestore")
public class ApiApplication {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiApplication.class);
	
	/**
	 * An Interface to write and read device information from/to gcp nosql firestore
	 */
	private DeviceRepo deviceRepo;

	/**
	 * Name of the gcp pub/sub topic where bbqb devices send messages to
	 */
	@Value("${bbq.backend.gcp.pubsub.incoming-topic}")
	private String pubSubIncomingTopic;

	/**
	 * Start the application as a Spring application and pass cmd arguments
	 * @param args Arguments passed to the application on startup
	 */
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
	

	/**
	 * Part of pub/sub subscription processing
	 * Provides a channel to which a ChannelAdapter sends received messages 
	 * @return A DirectChannel with default RoundRobinLoadBalancingStrategy
	 */
	@Bean
	public MessageChannel pubsubInputChannel() {
	  return new DirectChannel();
	}


	/**
	 * Part of pub/sub subscription processing
	 * Provides an Adapter which listens to a GCP Pub/Sub subscription to fetch incoming messages
	 * @param inputChannel to send received messages to
	 * @param pubSubTemplate Spring Template to communicate with gcp pub/sub
	 * @return A ChannelAdapter that provides messages from the topic pubSubIncomingTopic that have to be acknowledged manually
	 */
	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(@Qualifier("pubsubInputChannel") MessageChannel inputChannel,
	  PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, pubSubIncomingTopic); 
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);
		
		return adapter;
	}

	/**
	 * Part of pub/sub subscription processing
	 * @return A MessageHandler which processes incoming messages from an InputChannel and update device information with message content 
	 */
	@Bean
	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiver() {
		// TODO: Move this code to a separate file at a different abstraction level because it handles details like DB access
		return message -> {
			LOGGER.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
			//TODO: Write message to a time series database
			// Retrieve attributes and message body from message
			String deviceId = message.getHeaders().get("deviceId", String.class); //TODO: Move message attribute key to properties file
			String deviceStatus = message.getPayload().toString();
			Long deviceTimestamp = message.getHeaders().getTimestamp();

			// Update device object
			DeviceDoc deviceDoc = deviceRepo.findById(deviceId).block();
			deviceDoc.setPublishTime(String.valueOf(deviceTimestamp));
			deviceDoc.setStatus(deviceStatus);

			// Update device document in database
			deviceRepo.save(deviceDoc);
			
			// Acknowledge the message
			BasicAcknowledgeablePubsubMessage originalMessage =
			  message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
			originalMessage.ack();
		};
	}

}
