package org.delivery.api.domain.userorder.business;

import lombok.RequiredArgsConstructor;
import org.delivery.api.common.annotation.Business;
import org.delivery.api.domain.storemenu.service.StoreMenuService;
import org.delivery.api.domain.user.model.User;
import org.delivery.api.domain.userorder.controller.model.UserOrderRequest;
import org.delivery.api.domain.userorder.controller.model.UserOrderResponse;
import org.delivery.api.domain.userorder.converter.UserOrderConverter;
import org.delivery.api.domain.userorder.service.UserOrderService;
import org.delivery.api.domain.userordermenu.converter.UserOrderMenuConverter;
import org.delivery.api.domain.userordermenu.service.UserOrderMenuService;
import org.delivery.db.storemenu.StoreMenuEntity;
import org.delivery.db.userorder.UserOrderEntity;
import org.delivery.db.userordermenu.UserOrderMenuEntity;

import java.util.List;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Business
public class UserOrderBusiness {
    private final UserOrderService userOrderService;
    private final UserOrderMenuService userOrderMenuService;
    private final StoreMenuService storeMenuService;

    private final UserOrderConverter userOrderConverter;
    private final UserOrderMenuConverter userOrderMenuConverter;

    // 1. 사용자, 메뉴 id
    // 2. userOrder 생성
    // 3. UserOrderMenu 생성
    // 4. 응답 생성
    public UserOrderResponse userOrder(User user, UserOrderRequest body) {
        List<StoreMenuEntity> storeMenuEntityList = body.getStoreMenuIdList().stream()
                .map(it -> storeMenuService.getStoreMenuWithThrow(it))
                .collect(toList());

        // conveter 구성
        UserOrderEntity userOrderEntity = userOrderConverter.toEntity(user, storeMenuEntityList);
        // 주문
        UserOrderEntity newUserOderEntity = userOrderService.order(userOrderEntity);
        // 매핑
        List<UserOrderMenuEntity> userOrderMenuEntityList = storeMenuEntityList.stream()
                .map(it -> {
                    UserOrderMenuEntity userOrderMenuEntity = userOrderMenuConverter.toEntity(newUserOderEntity, it);
                    return userOrderMenuEntity;
                })
                .collect(toList());
        // 주문내역 기록 남기기
        userOrderMenuEntityList.forEach(it -> {
            userOrderMenuService.order(it);
        });

        return userOrderConverter.toResponse(newUserOderEntity);
    }


}
