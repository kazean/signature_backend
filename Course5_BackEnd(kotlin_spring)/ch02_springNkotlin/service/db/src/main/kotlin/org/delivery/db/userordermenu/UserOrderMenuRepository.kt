package org.delivery.db.userordermenu

import org.delivery.db.userordermenu.enums.UserOrderMenuStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserOrderMenuRepository : JpaRepository<UserOrderMenuEntity, Long> {
    fun findAllByUserOrderIdAndStatus(userOrderId: Long?, status: UserOrderMenuStatus?): List<UserOrderMenuEntity>
}