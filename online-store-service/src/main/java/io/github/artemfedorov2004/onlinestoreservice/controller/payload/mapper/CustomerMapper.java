package io.github.artemfedorov2004.onlinestoreservice.controller.payload.mapper;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.CustomerPayload;
import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper extends Mappable<Customer, CustomerPayload> {
}
