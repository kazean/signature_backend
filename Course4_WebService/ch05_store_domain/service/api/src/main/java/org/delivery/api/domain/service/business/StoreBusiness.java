package org.delivery.api.domain.service.business;

import lombok.RequiredArgsConstructor;
import org.delivery.api.common.annotation.Business;
import org.delivery.api.domain.service.controller.model.StoreRegisterRequest;
import org.delivery.api.domain.service.controller.model.StoreResponse;
import org.delivery.api.domain.service.converter.StoreConverter;
import org.delivery.api.domain.service.service.StoreService;
import org.delivery.db.store.StoreEntity;
import org.delivery.db.store.enums.StoreCategory;

import java.util.List;

import static java.util.stream.Collectors.*;

@Business
@RequiredArgsConstructor
public class StoreBusiness {
    private final StoreService storeService;
    private final StoreConverter storeConverter;

    public StoreResponse register(StoreRegisterRequest request) {
        var entity = storeConverter.toEntity(request);
        var newEntity = storeService.register(entity);
        var response = storeConverter.toResponse(newEntity);

        return response;
    }

    public List<StoreResponse> searchCategory(StoreCategory storeCategory) {
        List<StoreEntity> storeEntities = storeService.searchByCategory(storeCategory);
        return storeEntities.stream()
                .map(storeConverter::toResponse)
                .collect(toList());
    }

}
