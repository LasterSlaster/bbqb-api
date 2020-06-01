package de.bbqb.backend.gcp.firestore;

import org.springframework.cloud.gcp.data.firestore.FirestoreReactiveRepository;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import reactor.core.publisher.Flux;

/**
 * Read/Write device information from/to a google firestore NoSql database
 * 
 * @author laster
 *
 */
public interface DeviceRepo extends FirestoreReactiveRepository<DeviceDoc> {
	
	Flux<DeviceDoc> findAll();

	Flux<DeviceDoc> findById();
	
}
