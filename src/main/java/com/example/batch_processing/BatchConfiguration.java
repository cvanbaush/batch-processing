package com.example.batch_processing;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.example.batch_processing.news.AiClient;
import com.example.batch_processing.news.ArticleSummarizingProcessor;
import com.example.batch_processing.news.ClaudeClient;
import com.example.batch_processing.news.JsonLinesNewsReader;
import com.example.batch_processing.news.JsonLinesNewsWriter;
import com.example.batch_processing.news.NewsApiClient;
import com.example.batch_processing.news.NewsApiProperties;
import com.example.batch_processing.news.NewsArticle;
import com.example.batch_processing.news.NewsClient;
import com.example.batch_processing.news.NewsJobListener;
import com.example.batch_processing.news.NewsSummary;
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
    public Job fetchNewsJob(JobRepository jobRepository, Step fetchNewsStep, Step summarizeNewsStep, NewsJobListener newsJobListener) {
        return new JobBuilder(jobRepository)
            .listener(newsJobListener)
            .start(fetchNewsStep)
            .next(summarizeNewsStep)
            .build();
    }

    @Bean
    public AiClient aiClient(@Value("${anthropic.api-key}") String apiKey) {
        return new ClaudeClient(apiKey);
    }

    @Bean
    @StepScope
    public ItemReader<List<NewsArticle>> articleFileReader(
            @Value("#{jobParameters['keyword']}") String keyword) {
        String filename = String.format("data/%s-%s.jsonl", keyword, LocalDate.now());
        return new JsonLinesNewsReader(Path.of(filename));
    }

    @Bean
    @StepScope
    public ItemProcessor<List<NewsArticle>, NewsSummary> summarizingProcessor(
            AiClient aiClient,
            @Value("#{jobParameters['keyword']}") String keyword) {
        return new ArticleSummarizingProcessor(aiClient, keyword);
    }

    @Bean
    public ItemWriter<NewsSummary> summaryWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<NewsSummary>()
            .dataSource(dataSource)
            .sql("INSERT INTO news_summary (keyword, summary, processed_date) VALUES (:keyword, :summary, :processedDate)")
            .beanMapped()
            .build();
    }

    @Bean
    public Step summarizeNewsStep(JobRepository jobRepository,
                                  DataSourceTransactionManager transactionManager,
                                  ItemReader<List<NewsArticle>> articleFileReader,
                                  ItemProcessor<List<NewsArticle>, NewsSummary> summarizingProcessor,
                                  ItemWriter<NewsSummary> summaryWriter,
                                  NewsJobListener newsJobListener) {
        return new StepBuilder(jobRepository)
            .<List<NewsArticle>, NewsSummary>chunk(1)
            .transactionManager(transactionManager)
            .reader(articleFileReader)
            .processor(summarizingProcessor)
            .writer(summaryWriter)
            .listener(newsJobListener)
            .build();
    }
}

