package org.delivery.db.userorder;

import org.delivery.db.userorder.enums.UserOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserOrderRepository extends JpaRepository<UserOrderEntity, Long> {
    // 특정 유저의 모든 주문
    // select * from user_order where user_id = ? and status = ? orderBy id desc;
    List<UserOrderEntity> findAllByUserIdAndStatusOrderByIdDesc(long userId, UserOrderStatus status);

    // 특정 유저의 주문 중 특정 상태값들
    // select * from user_order where user_id = ? and status in (?, ...) order by id desc
    List<UserOrderEntity> findAllByUserIdAndStatusInOrderByIdDesc(long userId, List<UserOrderStatus> statuses);

    // 특정 주문
    Optional<UserOrderEntity> findFirstByIdAndStatusAndUserId(long id, UserOrderStatus status, long userId);
}
