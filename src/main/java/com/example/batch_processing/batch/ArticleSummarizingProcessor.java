package com.example.batch_processing.batch;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.batch_processing.client.AiClient;
import com.example.batch_processing.model.NewsArticle;
import com.example.batch_processing.model.NewsSummary;

public class ArticleSummarizingProcessor implements ItemProcessor<List<NewsArticle>, NewsSummary> {

    private final AiClient aiClient;
    private final String keyword;

    public ArticleSummarizingProcessor(AiClient aiClient, String keyword) {
        this.aiClient = aiClient;
        this.keyword = keyword;
    }

    @Override
    public NewsSummary process(List<NewsArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return new NewsSummary(keyword, "no articles", LocalDate.now());
        }

        String articlesText = articles.stream()
            .map(article -> String.format("Title: %s\nDescription: %s",
                article.title(),
                article.description()))
            .collect(Collectors.joining("\n\n"));

        String summary = aiClient.summarize(articlesText);

        return new NewsSummary(keyword, summary, LocalDate.now());
    }
}
