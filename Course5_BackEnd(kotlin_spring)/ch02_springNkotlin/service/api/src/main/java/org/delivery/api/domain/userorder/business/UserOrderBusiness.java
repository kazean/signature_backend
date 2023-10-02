package org.delivery.api.domain.userorder.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.delivery.common.annotation.Business;
import org.delivery.api.domain.store.converter.StoreConverter;
import org.delivery.api.domain.store.service.StoreService;
import org.delivery.api.domain.storemenu.converter.StoreMenuConverter;
import org.delivery.api.domain.storemenu.service.StoreMenuService;
import org.delivery.api.domain.userorder.controller.model.UserOrderDetailResponse;
import org.delivery.api.domain.userorder.converter.UserOrderConverter;
import org.delivery.api.domain.user.model.User;
import org.delivery.api.domain.userorder.controller.model.UserOrderRequest;
import org.delivery.api.domain.userorder.controller.model.UserOrderResponse;
import org.delivery.api.domain.userorder.producer.UserOrderProducer;
import org.delivery.api.domain.userorder.service.UserOrderService;
import org.delivery.api.domain.userordermenu.converter.UserOrderMenuConverter;
import org.delivery.api.domain.userordermenu.service.UserOrderMenuService;
import org.delivery.db.store.StoreEntity;
import org.delivery.db.storemenu.StoreMenuEntity;
import org.delivery.db.storemenu.enums.StoreMenuStatus;
import org.delivery.db.userorder.UserOrderEntity;
import org.delivery.db.userordermenu.UserOrderMenuEntity;
import org.delivery.db.userordermenu.enums.UserOrderMenuStatus;

import java.util.List;

import static java.util.stream.Collectors.*;

@Slf4j
@RequiredArgsConstructor
@Business
public class UserOrderBusiness {
    private final UserOrderService userOrderService;
    private final UserOrderConverter userOrderConverter;
    private final StoreMenuService storeMenuService;
    private final StoreMenuConverter storeMenuConverter;
    private final UserOrderMenuConverter userOrderMenuConverter;
    private final UserOrderMenuService userOrderMenuService;
    private final StoreService storeService;
    private final StoreConverter storeConverter;
    private final UserOrderProducer userOrderProducer;
    private final ObjectMapper objectMapper;

    // 1. 사용자, 메뉴 id
    // 2. userOrder 생성
    // 3. userOrderMenu 생성
    // 4. 응답 생성
    public UserOrderResponse userOrder(User user, UserOrderRequest body) {
        // add
        StoreEntity storeEntity = storeService.getStoreWithThrow(body.getStoreId());

        List<StoreMenuEntity> storeMenuEntityList = body.getStoreMenuIdList().stream()
                .map(it -> storeMenuService.getStoreMenuWithThrow(it))
                .collect(toList());

        UserOrderEntity userOrderEntity = userOrderConverter.toEntity(user, storeEntity, storeMenuEntityList);
        // 주문
        UserOrderEntity newUserOrderEntity = userOrderService.order(userOrderEntity);
        // 매핑
        List<UserOrderMenuEntity> userOrderMenuEntityList = storeMenuEntityList.stream()
                .map(it -> {
                    // menu + user order
                    UserOrderMenuEntity userOrderMenuEntity = userOrderMenuConverter.toEntity(newUserOrderEntity, it);
                    return userOrderMenuEntity;
                })
                .collect(toList());
        // 주문내역 기록 남기기
        userOrderMenuEntityList.forEach(it ->{
            userOrderMenuService.order(it);
        });

        // 비동기로 가맹점에 주문 알리기
        userOrderProducer.sendOrder(newUserOrderEntity);

        // response
        return userOrderConverter.toResponse(newUserOrderEntity);
    }

    public List<UserOrderDetailResponse> current(User user) {
        var userOrderEntityList = userOrderService.current(user.getId());
        // 주문 1건씩 처리
        var userOrderDetailResponseList = userOrderEntityList.stream()
                .map(userOrderEntity -> {
                    log.info("사용자 주문 {}", userOrderEntity);
                    try {
                        String jsonValue = objectMapper.writeValueAsString(userOrderEntity);
                        log.info("사용자 주문 JSON_VALUE {}", jsonValue);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // 사용자 주문 메뉴
//                    var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(it.getId());
                    var userOrderMenuEntityList = userOrderEntity.getUserOrderMenuList().stream()
                            .filter(it -> UserOrderMenuStatus.REGISTERED.equals(it.getStatus()))
                            .collect(toList());

                    var storeMenuEntityList = userOrderMenuEntityList.stream()
//                            .map(userOrderMenuEntity -> {
//                                var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenu().getId());
//                                return storeMenuEntity;
//                            })
                            .map(UserOrderMenuEntity::getStoreMenu)
                            .collect(toList());

                    // 사용자가 주문한 스토어 TODO 리팩토링 필요
//                    var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStore().getId());
                    var storeEntity = userOrderEntity.getStore();

                    return UserOrderDetailResponse.builder()
                            .userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
                            .storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
                            .storeResponse(storeConverter.toResponse(storeEntity))
                            .build();
                }).collect(toList());

        return userOrderDetailResponseList;
    }

    public List<UserOrderDetailResponse> history(User user) {
        var userOrderEntityList = userOrderService.history(user.getId());
        // 주문 1건씩 처리
        var userOrderDetailResponseList = userOrderEntityList.stream()
                .map(userOrderEntity -> {
                    // 사용자 주문 메뉴
//                    var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(it.getId());
                    var userOrderMenuEntityList = userOrderEntity.getUserOrderMenuList().stream()
                            .filter(it -> UserOrderMenuStatus.REGISTERED.equals(it.getStatus()))
                            .collect(toList());
                    var storeMenuEntityList = userOrderMenuEntityList.stream()
//                            .map(userOrderMenuEntity -> {
//                                var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenu().getId());
//                                return storeMenuEntity;
//                            })
                            .map(UserOrderMenuEntity::getStoreMenu)
                            .collect(toList());

                    // 사용자가 주문한 스토어 TODO 리팩토링 필요
//                    var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStore().getId());
                    var storeEntity = userOrderEntity.getStore();

                    return UserOrderDetailResponse.builder()
                            .userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
                            .storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
                            .storeResponse(storeConverter.toResponse(storeEntity))
                            .build();
                }).collect(toList());

        return userOrderDetailResponseList;
    }

    public UserOrderDetailResponse read(User user, Long orderId) {
        UserOrderEntity userOrderEntity = userOrderService.getUserOrderWithOutStatusWithThrow(orderId, user.getId());

//        var userOrderMenuEntityList = userOrderMenuService.getUserOrderMenu(userOrderEntity.getId());
        var userOrderMenuEntityList = userOrderEntity.getUserOrderMenuList().stream()
                .filter(it -> UserOrderMenuStatus.REGISTERED.equals(it.getStatus()))
                .collect(toList());

        var storeMenuEntityList = userOrderMenuEntityList.stream()
//                .map(userOrderMenuEntity -> {
//                    var storeMenuEntity = storeMenuService.getStoreMenuWithThrow(userOrderMenuEntity.getStoreMenu().getId());
//                    return storeMenuEntity;
//                })
                .map(UserOrderMenuEntity::getStoreMenu)
                .collect(toList());

        // 사용자가 주문한 스토어 TODO 리팩토링 필요
//        var storeEntity = storeService.getStoreWithThrow(storeMenuEntityList.stream().findFirst().get().getStore().getId());
        var storeEntity = userOrderEntity.getStore();

        return UserOrderDetailResponse.builder()
                .userOrderResponse(userOrderConverter.toResponse(userOrderEntity))
                .storeMenuResponseList(storeMenuConverter.toResponse(storeMenuEntityList))
                .storeResponse(storeConverter.toResponse(storeEntity))
                .build();

    }
}
