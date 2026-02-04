package com.example.batch_processing;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.example.batch_processing.news.JsonLinesNewsWriter;
import com.example.batch_processing.news.NewsApiProperties;
import com.example.batch_processing.news.NewsArticle;
import com.example.batch_processing.news.NewsJobListener;
import com.example.batch_processing.news.RestNewsReader;

import org.springframework.batch.core.job.Job;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class BatchConfiguration {


    @Bean
    public ItemReader<List<NewsArticle>> newsReader(
            NewsApiProperties newsApiProperties,
            @Value("${news.keyword}") String keyword) {
        return new RestNewsReader(newsApiProperties, keyword);
    }

    @Bean
    public ItemWriter<List<NewsArticle>> newsWriter(@Value("${news.keyword}") String keyword) {
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





    
    @Bean
    public FlatFileItemReader<Person> reader() {
    return new FlatFileItemReaderBuilder<Person>()
        .name("personItemReader")
        .resource(new ClassPathResource("sample-data.csv"))
        .delimited()
        .names("firstName", "lastName")
        .targetType(Person.class)
        .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Person>()
        .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
        .dataSource(dataSource)
        .beanMapped()
        .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1, JobCompletionNotificationListener listener) {
        return new JobBuilder(jobRepository)
            .listener(listener)
            .start(step1)
            .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
            FlatFileItemReader<Person> reader, PersonItemProcessor processor, JdbcBatchItemWriter<Person> writer) {

        return new StepBuilder(jobRepository)
            .<Person, Person>chunk(3)
                .transactionManager(transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}

