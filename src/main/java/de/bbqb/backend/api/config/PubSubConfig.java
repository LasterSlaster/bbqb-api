package de.bbqb.backend.api.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import de.bbqb.backend.gcp.firestore.DeviceMessageHandler;
import de.bbqb.backend.gcp.firestore.DeviceRepo;

/**
 * Configuration Class to configure spring context with beans etc. for gcp pupSub related instances/values
 * 
 * @author Marius Degen
 *
 */
//@Configuration
public class PubSubConfig {

	/**
	 * Name of the gcp pub/sub topic where bbqb devices send messages to
	 */
	@Value("${bbq.backend.gcp.pubsub.incoming-topic}")
	private String pubSubIncomingTopic;

	/**
	 * Name of the gcp pub/sub topic where bbqb devices send messages to
	 */
	@Value("${bbq.backend.gcp.pubsub.message-header-deviceid}")
	private String deviceIdMessageHeader;

	/**
	 * Part of pub/sub subscription processing Provides a channel to which a
	 * ChannelAdapter sends received messages
	 * 
	 * @return A DirectChannel with default RoundRobinLoadBalancingStrategy
	 */
	@Bean
	public MessageChannel pubsubInputChannel() {
		return new DirectChannel();
	}

	/**
	 * Part of pub/sub subscription processing Provides an Adapter which listens to
	 * a GCP Pub/Sub subscription to fetch incoming messages
	 * 
	 * @param inputChannel   to send received messages to
	 * @param pubSubTemplate Spring Template to communicate with gcp pub/sub
	 * @return A ChannelAdapter that provides messages from the topic
	 *         pubSubIncomingTopic that have to be acknowledged manually
	 */
	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier("pubsubInputChannel") MessageChannel inputChannel, PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, this.pubSubIncomingTopic);
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.MANUAL);

		return adapter;
	}
	
	/**
	 * Part of pub/sub subscription processing
	 * 
	 * @return A configured DeviceMessageHandler  
	 */
	@Bean
	public DeviceMessageHandler deviceMessageHandler(DeviceRepo deviceRepo) {
		return new DeviceMessageHandler(deviceRepo, this.deviceIdMessageHeader);
	}

	/**
	 * Part of pub/sub subscription processing
	 * 
	 * @return A MessageHandler which processes incoming messages from an
	 *         InputChannel and update device information with message content
	 */
	@Bean
	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiver(DeviceMessageHandler messageHandler) {
		return messageHandler;
	}
}
