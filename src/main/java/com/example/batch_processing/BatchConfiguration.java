package com.example.batch_processing;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.example.batch_processing.news.JsonLinesNewsWriter;
import com.example.batch_processing.news.NewsApiClient;
import com.example.batch_processing.news.NewsApiProperties;
import com.example.batch_processing.news.NewsArticle;
import com.example.batch_processing.news.NewsClient;
import com.example.batch_processing.news.NewsJobListener;
import com.example.batch_processing.news.RestNewsReader;

@Configuration
public class BatchConfiguration {

    @Bean
    public NewsClient newsClient(NewsApiProperties newsApiProperties) {
        return new NewsApiClient(newsApiProperties);
    }

    @Bean
    @StepScope
    public ItemReader<List<NewsArticle>> newsReader(
            NewsClient newsClient,
            @Value("#{jobParameters['keyword']}") String keyword) {
        return new RestNewsReader(newsClient, keyword);
    }

    @Bean
    @StepScope
    public ItemWriter<List<NewsArticle>> newsWriter(
            @Value("#{jobParameters['keyword']}") String keyword) {
        String filename = String.format("data/%s-%s.jsonl", keyword, LocalDate.now());
        return new JsonLinesNewsWriter(Path.of(filename));
    }

    @Bean
    public Step fetchNewsStep(JobRepository jobRepository,
                              DataSourceTransactionManager transactionManager,
                              ItemReader<List<NewsArticle>> newsReader,
                              ItemWriter<List<NewsArticle>> newsWriter,
                              NewsJobListener newsJobListener) {
        return new StepBuilder(jobRepository)
            .<List<NewsArticle>, List<NewsArticle>>chunk(1)
            .transactionManager(transactionManager)
            .reader(newsReader)
            .writer(newsWriter)
            .listener(newsJobListener)
            .build();
    }

    @Bean
    public Job fetchNewsJob(JobRepository jobRepository, Step fetchNewsStep, NewsJobListener newsJobListener) {
        return new JobBuilder(jobRepository)
            .listener(newsJobListener)
            .start(fetchNewsStep)
            .build();
    }
}

