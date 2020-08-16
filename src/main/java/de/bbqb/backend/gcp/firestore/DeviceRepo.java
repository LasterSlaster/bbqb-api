package de.bbqb.backend.gcp.firestore;

import org.springframework.cloud.gcp.data.firestore.FirestoreReactiveRepository;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import reactor.core.publisher.Flux;

/**
 * Read/Write device information from/to a gcp firestore NoSql database
 * An instance of this interface is provided by spring and injected into the context at runtime
 * 
 * @author laster
 *
 */
public interface DeviceRepo extends FirestoreReactiveRepository<DeviceDoc> {
	
	Flux<DeviceDoc> findAll();

	Flux<DeviceDoc> findById();
	
}
