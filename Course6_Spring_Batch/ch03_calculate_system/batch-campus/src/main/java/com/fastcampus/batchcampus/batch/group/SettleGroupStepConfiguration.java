package com.fastcampus.batchcampus.batch.group;

import com.fastcampus.batchcampus.domain.Customer;
import com.fastcampus.batchcampus.domain.SettleGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SettleGroupStepConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    public Step settleGroupStep(
            SettleGroupItemReader settleGroupItemReader,
            SettleGroupItemProcessor settleGroupItemProcessor,
            ItemWriter<List<SettleGroup>> settleGroupItemWriter
    ) {
        return new StepBuilder("settleGroupStep", jobRepository)
                .<Customer, List<SettleGroup>>chunk(100, platformTransactionManager)
                .reader(settleGroupItemReader)
                .processor(settleGroupItemProcessor)
                .writer(settleGroupItemWriter)
                .build();
    }

    @Bean
    public ItemWriter<List<SettleGroup>> settleGroupItemWriter(
            SettleGroupItemDbWriter settleGroupItemDbWriter,
            SettleGroupItemMailWriter settleGroupItemMailWriter
    ) {
        return new CompositeItemWriter<>(
                settleGroupItemDbWriter,
                settleGroupItemMailWriter
        );
    }
}
