package org.delivery.api.domain.userorder.business

import org.delivery.api.common.Log
import org.delivery.api.domain.store.converter.StoreConverter
import org.delivery.api.domain.store.service.StoreService
import org.delivery.api.domain.storemenu.converter.StoreMenuConverter
import org.delivery.api.domain.storemenu.service.StoreMenuService
import org.delivery.api.domain.user.model.User
import org.delivery.api.domain.userorder.controller.model.UserOrderDetailResponse
import org.delivery.api.domain.userorder.controller.model.UserOrderRequest
import org.delivery.api.domain.userorder.controller.model.UserOrderResponse
import org.delivery.api.domain.userorder.converter.UserOrderConverter
import org.delivery.api.domain.userorder.producer.UserOrderProducer
import org.delivery.api.domain.userorder.service.UserOrderService
import org.delivery.api.domain.userordermenu.converter.UserOrderMenuConverter
import org.delivery.api.domain.userordermenu.service.UserOrderMenuService
import org.delivery.common.annotation.Business
import org.delivery.db.userordermenu.UserOrderMenuEntity
import org.delivery.db.userordermenu.enums.UserOrderMenuStatus
import kotlin.streams.toList

@Business
class UserOrderBusiness (
    private val userOrderService: UserOrderService,
    private val userOrderConverter: UserOrderConverter,

    private val storeMenuService: StoreMenuService,
    private val storeMenuConverter: StoreMenuConverter,

    private val userOrderMenuConverter: UserOrderMenuConverter,
    private val userOrderMenuService: UserOrderMenuService,

    private val storeService: StoreService,
    private val storeConverter: StoreConverter,
    private val userOrderProducer: UserOrderProducer,
) {
    companion object Log
    fun userOrder(
        user:User?,
        body:UserOrderRequest?
    ) : UserOrderResponse {
        // 가게찾기
        val storeEntity = storeService.getStoreWithThrow(body?.storeId);

        // 주문한 메뉴 찾기
        val storeMenuEntityList = body?.storeMenuIdList
            ?.mapNotNull { storeMenuService.getStoreMenuWithThrow(it) }
            ?.toList()

        // 주문메뉴 변환, 주문
        val userOrderEntity = userOrderConverter.toEntity(user, storeEntity, storeMenuEntityList)
            .run { userOrderService.order(this) }
//            .let { userOrderService.order(it) }
        // 매핑 후 주문내역 기록 남기기, userOrderMenuList
        storeMenuEntityList
            ?.map { userOrderMenuConverter.toEntity(userOrderEntity, it) }
            ?.forEach { userOrderMenuService.order(it) }

        // 비동기로 가맹점에 주문 알리기
        userOrderProducer.sendOrder(userOrderEntity)
        return userOrderConverter.toResponse(userOrderEntity)
    }

    fun current(
        user: User?
    ) :List<UserOrderDetailResponse>? {
        return userOrderService.current(user?.id)
            .map { userOrderEntity ->
                val storeMenuEntityList = userOrderEntity.userOrderMenuList
                    ?.filter { UserOrderMenuStatus.REGISTERED == it.status }
                    ?.map {
                        it.storeMenu
                    }
                    ?.toList()

                val storeEntity = userOrderEntity.store
                UserOrderDetailResponse(
                    userOrderResponse = userOrderConverter.toResponse(userOrderEntity),
                    storeResponse = storeConverter.toResponse(storeEntity),
                    storeMenuResponseList = storeMenuConverter.toResponse(storeMenuEntityList)

                )
            }
    }

    fun history(
        user: User?
    ) :List<UserOrderDetailResponse>? {
        return userOrderService.history(user?.id)
            .map { userOrderEntity ->
                val storeMenuEntityList = userOrderEntity.userOrderMenuList
                    ?.filter { UserOrderMenuStatus.REGISTERED == it.status }
                    ?.map {
                        it.storeMenu
                    }
                    ?.toList()

                val storeEntity = userOrderEntity.store
                UserOrderDetailResponse(
                    userOrderResponse = userOrderConverter.toResponse(userOrderEntity),
                    storeResponse = storeConverter.toResponse(storeEntity),
                    storeMenuResponseList = storeMenuConverter.toResponse(storeMenuEntityList)

                )
            }
    }
    
    fun read(
        user:User?,
        orderId:Long?
    ): UserOrderDetailResponse {
        val userOrderEntity = userOrderService.getUserOrderWithOutStatusWithThrow(orderId, user?.id)
        val storeMenuEntityList = userOrderEntity.userOrderMenuList
            ?.filter { UserOrderMenuStatus.REGISTERED == it.status }
            ?.map(UserOrderMenuEntity::getStoreMenu)
            ?.toList()
            ?: listOf()
        return UserOrderDetailResponse(
            userOrderResponse = userOrderConverter.toResponse(userOrderEntity),
            storeResponse = storeConverter.toResponse(userOrderEntity.store),
            storeMenuResponseList = storeMenuConverter.toResponse(storeMenuEntityList
            )

        )
    }
}