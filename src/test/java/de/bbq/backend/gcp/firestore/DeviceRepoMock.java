package de.bbq.backend.gcp.firestore;

import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DeviceRepoMock implements DeviceRepo {

    @Override
    public <S extends DeviceDoc> Mono<S> save(S entity) {
        // TODO Auto-generated method stub
        return Mono.just(entity);
    }

    @Override
    public <S extends DeviceDoc> Flux<S> saveAll(Iterable<S> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <S extends DeviceDoc> Flux<S> saveAll(Publisher<S> entityStream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<DeviceDoc> findById(String id) {
        // TODO Auto-generated method stub
        return Mono.just(new DeviceDoc());
    }

    @Override
    public Mono<DeviceDoc> findById(Publisher<String> id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Boolean> existsById(Publisher<String> id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flux<DeviceDoc> findAll() {
        // TODO Auto-generated method stub
        return Flux.just(new DeviceDoc());
    }

    @Override
    public Flux<DeviceDoc> findAllById(Iterable<String> ids) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flux<DeviceDoc> findAllById(Publisher<String> idStream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Long> count() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Void> deleteById(String id) {
        // TODO Auto-generated method stub
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteById(Publisher<String> id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Void> delete(DeviceDoc entity) {
        // TODO Auto-generated method stub
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends DeviceDoc> entities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends DeviceDoc> entityStream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<Void> deleteAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mono<DeviceDoc> findFirstByDeviceId(String id) {
        // TODO Auto-generated method stub
        return null;
    }

}
