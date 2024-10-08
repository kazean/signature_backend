package org.delivery.storeadmin.domain.userorder.consumer;

import lombok.extern.slf4j.Slf4j;
import org.delivery.common.message.model.UserOrderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserOrderConsumer {
    @RabbitListener(queues = "delivery.queue")
    public void userOrderConsumer(
            UserOrderMessage userOrderMessage
    ) {
        log.info("messgae queue >> {}", userOrderMessage);
    }
}
