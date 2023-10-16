package com.fastcampus.batchcampus;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class ItemReaderJobConfiguration {

    @Bean
    public Job job(
            JobRepository jobRepository,
            Step step
    ) {
        return new JobBuilder("itemReaderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager,
//            ItemReader<User> flatFileItemReader
//            ItemReader<User> fixedLengthFlatFileItemReader
//            ItemReader<User> jsonItemReader
//            ItemReader<User> jpaPagingItemReader
            ItemReader<User> jpaCursorItemReader
    ) {
        return new StepBuilder("step", jobRepository)
                .<User, User>chunk(2, platformTransactionManager)
                .reader(jpaCursorItemReader)
                .writer(System.out::println)
                .build();
    }

    @Bean
    public FlatFileItemReader<User> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("flatFileItemReader")
                .resource(new ClassPathResource("users.txt"))
                .linesToSkip(2)
                .delimited().delimiter("|") // default ','
                .names("name", "age", "region", "telephone")
                .targetType(User.class)
                .strict(true) // default: true, false > 읽지안고 정상적으로 종료
                .build();
    }

    // 옛날 방식이지만 종종 사용됨
    @Bean
    public FlatFileItemReader<User> fixedLengthFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("fixedLengthFlatFileItemReader")
                .resource(new ClassPathResource("usersFixedLength.txt"))
                .linesToSkip(2)
                .fixedLength()
                .columns(new Range[]{new Range(1,2), new Range(3,4), new Range(5,6), new Range(7,19)})
                .names("name", "age", "region", "telephone")
                .targetType(User.class)
                .strict(true) // default: true, false > 읽지안고 정상적으로 종료
                .build();
    }

    @Bean
    public ItemReader<User> jsonItemReader() {
        return new JsonItemReaderBuilder<User>()
                .name("jsonItemReader")
                .resource(new ClassPathResource("users.json"))
                .jsonObjectReader(new JacksonJsonObjectReader<>(User.class))
                .build();
    }

    @Bean
    public ItemReader<User> jpaPagingItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaPagingItemReaderBuilder<User>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(3)
                .queryString("SELECT u From User u Order By u.id")
                .build();
    }

    @Bean
    public ItemReader<User> jpaCursorItemReader(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaCursorItemReaderBuilder<User>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u From User u Order By u.id")
                .build();
    }
}
