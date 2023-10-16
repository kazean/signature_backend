package com.fastcampus.batchcampus;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
//@Configuration
public class MultiThreadJobConfiguration {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ) {
        return new JobBuilder("multiThreadJob", jobRepository)
                .start(step)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
            JpaPagingItemReader pagingItemReader
    ) {
        return new StepBuilder("step", jobRepository)
                .<User, User>chunk(5, platformTransactionManager)
                .reader(pagingItemReader)
                .writer(result -> log.info(result.toString()))
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> pagingItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<User>()
                .name("pagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(2)
                .saveState(false) // 실패시 재시작 지점을 알 수 없기에 false 권장
                .queryString("SELECT u FROM User u ORDER BY u.id")
                .build();
    }
}
