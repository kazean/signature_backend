package org.delivery.api.domain.userorder.producer;

import lombok.RequiredArgsConstructor;
import org.delivery.api.common.rabbitmq.Producer;
import org.delivery.db.userorder.UserOrderEntity;
import org.devliery.common.message.model.UserOrderMessage;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserOrderProducer {
    private final Producer producer;
    private final static String EXCHANGE = "delivery.exchange";
    private final static String ROUTE_KEY = "delivery.key";

    public void sendOrder(UserOrderEntity userOrderEntity) {
        sendOrder(userOrderEntity.getId());
    }

    public void sendOrder(Long userOrderId) {
        UserOrderMessage message = UserOrderMessage.builder()
                .userOrderId(userOrderId)
                .build();
        producer.producer(EXCHANGE, ROUTE_KEY, message );
    }
}
