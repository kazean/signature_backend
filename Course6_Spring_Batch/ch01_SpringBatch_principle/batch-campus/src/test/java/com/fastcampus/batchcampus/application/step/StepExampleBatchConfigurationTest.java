package com.fastcampus.batchcampus.application.step;

import com.fastcampus.batchcampus.batch.Job;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StepExampleBatchConfigurationTest {
    @Autowired
    private Job stepExampleBatchJob;

    @Test
    @DisplayName("StepJobTest")
    void test() {
        stepExampleBatchJob.execute();
    }

}