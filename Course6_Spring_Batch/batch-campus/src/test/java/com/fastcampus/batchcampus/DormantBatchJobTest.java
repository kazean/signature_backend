package com.fastcampus.batchcampus;

import com.fastcampus.batchcampus.batch.BatchStatus;
import com.fastcampus.batchcampus.batch.Job;
import com.fastcampus.batchcampus.batch.JobExecution;
import com.fastcampus.batchcampus.batch.TaskletJob;
import com.fastcampus.batchcampus.customer.Customer;
import com.fastcampus.batchcampus.customer.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class DormantBatchJobTest {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private Job dormantBatchJob;

    @Test
    @DisplayName("로그인 시간이 일년을 경과한 고객이 세명이고, 일년 이내에 로그인한 고객이 다섯명이면 3명의 고객이 휴면전환대상이다")
    @Transactional
    void test1() {
        // given
        saveCustomer(366);
        saveCustomer(366);
        saveCustomer(366);

        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);
        saveCustomer(364);

        // when
        final JobExecution result = dormantBatchJob.execute();

        // then
        long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        assertThat(dormantCount).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }


    @Test
    @DisplayName("고객이 열명이 있지만 모두다 휴면전환대상이면 휴면전환 대상은 10명이다.")
    @Transactional
    void test2() {
        // given
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);
        saveCustomer(400);

        // when
        final JobExecution result = dormantBatchJob.execute();

        //then
        long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        assertThat(dormantCount).isEqualTo(10);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("고객이 없는 경우에도 배치는 정상동작해야한다")
    @Transactional
    void test3() {
        // when
        final JobExecution result = dormantBatchJob.execute();

        //then
        long dormantCount = customerRepository.findAll()
                .stream()
                .filter(it -> it.getStatus() == Customer.Status.DORMANT)
                .count();

        assertThat(dormantCount).isEqualTo(0);
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("배치가 실패하면 BatchStatus는 FAILED를 반환해야한다.")
    @Transactional
    void test4() {
        // given
        final Job dormantBatchJob = new TaskletJob(null);

        // when
        final JobExecution result = dormantBatchJob.execute();

        // then
        assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    @Test
    @DisplayName("358전에 로그인한 고객에게 휴먼계정 예장자라고 메일을 발송해야한다")
    @Transactional
    void test5() {
        // given
        saveCustomer(358);
        saveCustomer(358);
        saveCustomer(358);
        saveCustomer(35);
        saveCustomer(35);

        // when
        // then
        dormantBatchJob.execute();
    }

    private void saveCustomer(long loginMinusDays) {
        final String uuid = UUID.randomUUID().toString();

        final Customer test = new Customer(uuid, uuid + "@fastcampus.com");
        test.setLoginAt(LocalDateTime.now().minusDays(loginMinusDays));
        customerRepository.save(test);
    }
}