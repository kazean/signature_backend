package com.fastcampus.batchcampus.application.dormant;

import com.fastcampus.batchcampus.customer.Customer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PreDormantBatchItemProcessorTest {
    private PreDormantBatchItemProcessor preDormantBatchItemProcessor;

    @BeforeEach
    void setup() {
        preDormantBatchItemProcessor = new PreDormantBatchItemProcessor();
    }

    @Test
    @DisplayName("로그인 날짜가 오늘로부터 358일 전이면 customer를 반환해야 한다.")
    void test1() {
        // given
        final Customer customer = new Customer("minsoo", "minsoo@fastcampus.com");
        // 오늘 날짜는 2023.10.11 예정자는 2022.10.18
        customer.setLoginAt(LocalDateTime.now().minusDays(365).plusDays(7));

        // when
        final Customer result = preDormantBatchItemProcessor.process(customer);

        // then
        Assertions.assertThat(result).isEqualTo(customer);
        Assertions.assertThat(result).isNotNull();

    }

    @Test
    @DisplayName("로그인 날짜가 오늘로부터 358일 전이 아니면 null을 반환해야한다.")
    void test2() {
        // given
        final Customer customer = new Customer("minsoo", "minsoo@fastcampus.com");

        // when
        final Customer result = preDormantBatchItemProcessor.process(customer);

        // then
        Assertions.assertThat(result).isNull();
    }
}