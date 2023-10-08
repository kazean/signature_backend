package org.delivery.api.domain.storemenu.converter;

import org.delivery.common.annotation.Converter;
import org.delivery.common.error.ErrorCode;
import org.delivery.common.exception.ApiException;
import org.delivery.api.domain.storemenu.controller.model.StoreMenuRegisterRequest;
import org.delivery.api.domain.storemenu.controller.model.StoreMenuResponse;
import org.delivery.db.store.StoreEntity;
import org.delivery.db.storemenu.StoreMenuEntity;
import org.delivery.db.storemenu.enums.StoreMenuStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Converter
public class StoreMenuConverter {
    public StoreMenuEntity toEntity(
            StoreEntity storeEntity,
            StoreMenuRegisterRequest request
    ) {
        return Optional.ofNullable(request)
                .map(it -> {
                    return StoreMenuEntity.builder()
//                            .storeId(request.getStoreId())
                            .store(storeEntity)
                            .name(it.getName())
                            .amount(it.getAmount())
                            .thumbnailUrl(it.getThumbnailUrl())
                            .status(StoreMenuStatus.REGISTERED)
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public StoreMenuResponse toResponse(StoreMenuEntity entity) {
        return Optional.ofNullable(entity)
                .map(it -> {
                    return StoreMenuResponse.builder()
                            .id(it.getId())
//                            .storeId(it.getStoreId())
                            .storeId(entity.getStore().getId())
                            .name(it.getName())
                            .amount(it.getAmount())
                            .status(it.getStatus())
                            .thumbnailUrl(it.getThumbnailUrl())
                            .likeCount(it.getLikeCount())
                            .sequence(it.getSequence())
                            .build();
                })
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));
    }

    public List<StoreMenuResponse> toResponse(
            List<StoreMenuEntity> list
    ) {
        return list.stream()
                .map(it -> {
                    return toResponse(it);
                }).collect(toList());
    }
}
