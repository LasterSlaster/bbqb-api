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

/**
 * Handle incoming bbqb device messages from gcp pubSub
 * 
 * @author laster
 *
 */
public class DeviceMessageHandler implements MessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMessageHandler.class);

	// TODO: Move all @Value annotation to spring context
	// configuration class and inject values from there
	/**
	 * Name of the gcp pub/sub topic where bbqb devices send messages to
	 */
	@Value("${bbq.backend.gcp.pubsub.message-header-deviceid}") 
	private String deviceIdMessageHeader;

	/**
	 * Repo to write and read device information from/to gcp nosql firestore
	 */
	private final DeviceRepo deviceRepo;

	public DeviceMessageHandler(DeviceRepo deviceRepo) {
		this.deviceRepo = deviceRepo;
	}

	/**
	 * Handle bbqb device status message by updating device document with the status information from the message
	 */
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		LOGGER.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
		// Retrieve attributes and message body from message
		String deviceId = message.getHeaders().get(deviceIdMessageHeader, String.class);
		String deviceStatus = message.getPayload().toString();
		Long deviceTimestamp = message.getHeaders().getTimestamp();

		if (deviceRepo == null) {
			throw new MessagingException("!!!!!!!!!!!!!! Repo is null!!!!!!!");
			// TODO: Implement logic to handle this case more appropriately!
		}
		// Update device
		deviceRepo.findById(deviceId).flatMap(device -> {
			device.setPublishTime(String.valueOf(deviceTimestamp));
			device.setStatus(deviceStatus);

			// Update device document in database
			return deviceRepo.save(device);
		}).subscribe(a -> {
			// Acknowledge the message
			BasicAcknowledgeablePubsubMessage originalMessage = message.getHeaders()
					.get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
			originalMessage.ack();
		}); // If no Device is found do nothing. In such cases the message is not
			// acknowledged
	}

}