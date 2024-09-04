package org.delivery.api.domain.service.controller;

import lombok.RequiredArgsConstructor;
import org.delivery.api.common.api.Api;
import org.delivery.api.domain.service.business.StoreBusiness;
import org.delivery.api.domain.service.controller.model.StoreRegisterRequest;
import org.delivery.api.domain.service.controller.model.StoreResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-api/store")
public class StoreOpenApiController {
    private final StoreBusiness storeBusiness;

    @PostMapping("/register")
    public Api<StoreResponse> register(
            @RequestBody @Valid
            Api<StoreRegisterRequest> request
    ) {
        var response = storeBusiness.register(request.getBody());
        return Api.OK(response);
    }
}
