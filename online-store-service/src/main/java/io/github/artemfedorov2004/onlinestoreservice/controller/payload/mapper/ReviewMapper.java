package io.github.artemfedorov2004.onlinestoreservice.controller.payload.mapper;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewReviewPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.ReviewPayload;
import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.StreamSupport;

@Mapper(componentModel = "spring")
public abstract class ReviewMapper implements Mappable<Review, ReviewPayload> {

    @Autowired
    protected CustomerMapper customerMapper;

    public abstract Review fromPayload(NewReviewPayload payload);

    @Override
    @Mapping(target = "createdBy", expression = "java(this.customerMapper.fromPayload(payload.createdBy()))")
    public abstract Review fromPayload(ReviewPayload payload);

    @Override
    @Mapping(target = "createdBy", expression = "java(this.customerMapper.toPayload(entity.getCreatedBy()))")
    public abstract ReviewPayload toPayload(Review entity);

    @Override
    public Iterable<ReviewPayload> toPayload(Iterable<Review> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::toPayload)
                .toList();
    }
}
