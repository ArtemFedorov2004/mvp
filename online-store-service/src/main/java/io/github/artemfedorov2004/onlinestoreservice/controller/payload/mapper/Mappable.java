package io.github.artemfedorov2004.onlinestoreservice.controller.payload.mapper;

public interface Mappable<E, P> {

    E fromPayload(P payload);

    P toPayload(E entity);

    Iterable<P> toPayload(Iterable<E> entities);
}
