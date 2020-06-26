package de.bbq.backend.gcp.firestore;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.concurrent.ListenableFuture;

import com.google.api.client.util.ArrayMap;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import de.bbqb.backend.gcp.firestore.DeviceMessageHandler;
import de.bbqb.backend.gcp.firestore.DeviceRepo;

public class DeviceMessageHandlerTest {
	
	private DeviceMessageHandler sut;
	
	private DeviceRepo deviceRepoMock;

	private final String deviceIdMessageHeader = "DeviceIdHeader";
	
	private final String deviceId = "deviceId";

	private final String deviceStatusPayload = "device status";
	
	@BeforeEach
	public void setUp() {
		this.deviceRepoMock = new DeviceRepoMock(); 

		this.sut = new DeviceMessageHandler(deviceRepoMock, deviceIdMessageHeader);
	}

	@Test
	public void testWriteValidMessageToRepo() {
		// given
		Map<String, Object> headers = new ArrayMap<>();
		headers.put(deviceIdMessageHeader, deviceId);
		headers.put(GcpPubSubHeaders.ORIGINAL_MESSAGE, new BasicAcknowledgeablePubsubMessageMock());
		MessageHeaders messageHeaders = new MessageHeaders(headers);
		Message<Object> message = new MessageMock(deviceStatusPayload, messageHeaders);
		
		// when
		this.sut.handleMessage(message);

		// then
		// validate that a message was sent to the repo to update the device document specified by the deviceId
	}
	
	@Test
	public void testMissingDeviceRepo() {
		// given
		
		// when
		//this.sut.handleMessage(message);

		// then
		// validate that a message was sent to the repo to update the device document specified by the deviceId
	}
	
	@Test
	public void testInvalidOrUnknownDevice() {
		// given
		
		// when
		//this.sut.handleMessage(message);

		// then
		// validate that a message was sent to the repo to update the device document specified by the deviceId
	}

	@Test
	public void testMissingMessagePayload() {
		// given
		
		// when
		//this.sut.handleMessage(message);

		// then
		// validate that a message was sent to the repo to update the device document specified by the deviceId
	}

	@Test
	public void testMissingMessageHeaders() {
		// given
		
		// when
		//this.sut.handleMessage(message);

		// then
		// validate that a message was sent to the repo to update the device document specified by the deviceId
	}

	/**
	 * Mock to test Message arguments.
	 * TODO: Check if it makes sense to use a junit spy or something
	 * @author Marius Degen
	 *
	 */
	private class MessageMock implements Message<Object> {
		
		private Object payload;
		private MessageHeaders headers;
		
		public MessageMock(Object payload, MessageHeaders headers) {
			this.payload = payload;
			this.headers = headers;
		}

		@Override
		public Object getPayload() {
			return payload;
		}

		@Override
		public MessageHeaders getHeaders() {
			return headers;
		}
		
	}
	
	/**
	 * Mock with unimplemented Methods
	 * TODO: Check if it makes sense to use a spy instead
	 * 
	 * @author Marius Degen
	 *
	 */
	private class BasicAcknowledgeablePubsubMessageMock implements BasicAcknowledgeablePubsubMessage {

		@Override
		public ProjectSubscriptionName getProjectSubscriptionName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PubsubMessage getPubsubMessage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ListenableFuture<Void> ack() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ListenableFuture<Void> nack() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}