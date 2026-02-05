package com.example.batch_processing.client;

import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import com.example.batch_processing.config.NewsApiProperties;
import com.example.batch_processing.model.NewsApiResponse;
import com.example.batch_processing.model.NewsArticle;

public class NewsApiClient implements NewsClient {

    private final NewsApiProperties properties;
    private final RestClient restClient;

    public NewsApiClient(NewsApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .build();
    }

    NewsApiClient(NewsApiProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public List<NewsArticle> fetchArticles(String keyword) {
        List<NewsArticle> articles = new ArrayList<>();

        NewsApiResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/top-headlines")
                .queryParam("q", keyword)
                .queryParam("pageSize", properties.getPageSize())
                .queryParam("apiKey", properties.getApiKey())
                .build())
            .retrieve()
            .body(NewsApiResponse.class);

        if (response == null || response.articles() == null) {
            return articles;
        }

        for (NewsApiResponse.Article article : response.articles()) {
            String sourceName = article.source() != null ? article.source().name() : null;
            articles.add(new NewsArticle(
                sourceName,
                article.author(),
                article.title(),
                article.description()
            ));
        }

        return articles;
    }
}
