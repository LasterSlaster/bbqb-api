package de.bbqb.backend.gcp.firestore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import de.bbqb.backend.gcp.firestore.document.DeviceDoc;

public class DeviceMessageHandler implements MessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMessageHandler.class);
	
	/**
	 * Name of the gcp pub/sub topic where bbqb devices send messages to
	 */
	@Value("${bbq.backend.gcp.pubsub.message-header-deviceid}")
	private String deviceIdMessageHeader;

	/**
	 * An Interface to write and read device information from/to gcp nosql firestore
	 */
	private DeviceRepo deviceRepo;

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		LOGGER.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
			// Retrieve attributes and message body from message
			String deviceId = message.getHeaders().get(deviceIdMessageHeader, String.class);
			String deviceStatus = message.getPayload().toString();
			Long deviceTimestamp = message.getHeaders().getTimestamp();

			// Update device object
			DeviceDoc deviceDoc = deviceRepo.findById(deviceId).block(); // TODO: CHeck what happens if no device is found and how to handle these cases
			deviceDoc.setPublishTime(String.valueOf(deviceTimestamp));
			deviceDoc.setStatus(deviceStatus);

			// Update device document in database
			deviceRepo.save(deviceDoc);
			
			// Acknowledge the message
			BasicAcknowledgeablePubsubMessage originalMessage =
			  message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
			originalMessage.ack();
	}

}
