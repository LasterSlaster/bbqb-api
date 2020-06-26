package de.bbqb.backend.gcp.firestore;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import com.google.cloud.Timestamp;

/**
 * Handle incoming bbqb device messages from gcp pubSub
 * 
 * @author Marius Degen
 *
 */
public class DeviceMessageHandler implements MessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceMessageHandler.class);

	/**
	 * Name of the gcp pub/sub topic where bbqb devices send messages to
	 */
	private String deviceIdMessageHeader;

	/**
	 * Repo to write and read device information from/to gcp nosql firestore
	 */
	private final DeviceRepo deviceRepo;

	public DeviceMessageHandler(DeviceRepo deviceRepo, String deviceIdMessageHeader) {
		this.deviceRepo = deviceRepo;
		this.deviceIdMessageHeader = deviceIdMessageHeader;
	}

	/**
	 * Handle bbqb device status message by updating device document with the status
	 * information from the message
	 */
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		//LOGGER.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
		// Retrieve attributes and message body from message
		String deviceId = message.getHeaders().get(this.deviceIdMessageHeader, String.class);
		String deviceStatus = message.getPayload().toString();
		Long deviceTimestamp = message.getHeaders().getTimestamp();

		if (this.deviceRepo == null) {
			throw new MessagingException("!!!!!!!!!!!!!! Repo is null!!!!!!!");
			// TODO: Implement logic to handle this case more appropriately!
		} else {
			// Update device
			this.deviceRepo.findById(deviceId).flatMap(device -> {
				device.setPublishTime(Timestamp.of(new Date(deviceTimestamp)));
				device.setStatus(deviceStatus);

				// Update device document in database
				return this.deviceRepo.save(device);
			}).subscribe(a -> {
				// Acknowledge the message
				BasicAcknowledgeablePubsubMessage originalMessage = message.getHeaders()
						.get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
				originalMessage.ack();
			}); // If no Device is found do nothing. In such cases the message is not
				// acknowledged
		}
	}

}