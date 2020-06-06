package de.bbqb.backend.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@SpringBootApplication
@EnableReactiveFirestoreRepositories("de.bbqb.backend.gcp.firestore")
public class ApiApplication {
	
	private static final Log LOGGER = LogFactory.getLog(ApiApplication.class);
	
	private DeviceRepo deviceRepo;

	@Value("${bbqb.gcp.pubsub.name}")
	private String pubSubName;


	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
	

	/**
	 * Provides a channel to which a ChannelAdapter sends received messages 
	 * @return
	 */
	@Bean
	@ServiceActivator(inputChannel = "pubsubOutputChannel")
		public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
		return new PubSubMessageHandler(pubsubTemplate, "testTopic"); // TODO: Change topic!
	}

	@MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
	public interface PubsubOutboundGateway {
		void sendToPubsub(String text);
	}

	/**
	 * Provides a channel to which a ChannelAdapter sends received messages 
	 * @return
	 */
	@Bean
	public MessageChannel pubsubInputChannel() {
	  return new DirectChannel();
	}


	/**
	 * Provides an Adapter which listens to a GCP Pub/Sub subscription
	 * @param inputChannel
	 * @param pubSubTemplate
	 * @return
	 */
	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
	  @Qualifier("pubsubInputChannel") MessageChannel inputChannel,
	  PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter =
			  new PubSubInboundChannelAdapter(pubSubTemplate, pubSubName); 
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);
		
		return adapter;
	}

	/**
	 * Processes incoming messages from an InputChannel
	 * @return
	 */
	@Bean
	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiver() {
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
