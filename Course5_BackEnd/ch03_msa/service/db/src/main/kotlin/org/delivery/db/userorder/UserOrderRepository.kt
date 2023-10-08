package org.delivery.db.userorder

import org.delivery.db.userorder.enums.UserOrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserOrderRepository : JpaRepository<UserOrderEntity, Long> {
    // 특정 유저의 모든 주문
    fun findAllByUserIdAndStatusOrderByIdDesc(userId: Long?, status: UserOrderStatus?): List<UserOrderEntity>
    fun findAllByUserIdAndStatusInOrderByIdDesc(userId: Long?, status: List<UserOrderStatus>?): List<UserOrderEntity>

    // 특정 주문
    fun findAllByIdAndStatusAndUserId(id: Long?, status: UserOrderStatus?, userId: Long?): UserOrderEntity?
    fun findAllByIdAndUserId(id: Long?, userId: Long?): UserOrderEntity?
}