package com.example.batch_processing.news;

import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RestNewsReader implements ItemReader<List<NewsArticle>> {

    private final NewsApiProperties properties;
    private final RestClient restClient;
    private final String keyword;

    private boolean read = false;

    public RestNewsReader(NewsApiProperties properties, String keyword) {
        this(properties, keyword, RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .build());
    }

    RestNewsReader(NewsApiProperties properties, String keyword, RestClient restClient) {
        this.properties = properties;
        this.keyword = keyword;
        this.restClient = restClient;
    }

    @Override
    public List<NewsArticle> read() {
        if (read) {
            return null;
        }
        read = true;
        return fetchAllArticles();
    }

    private List<NewsArticle> fetchAllArticles() {
        List<NewsArticle> allArticles = new ArrayList<>();
        String fromDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        int page = 1;
        int totalResults = 0;

        do {
            final int currentPage = page;
            NewsApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/everything")
                    .queryParam("q", keyword)
                    .queryParam("from", fromDate)
                    .queryParam("pageSize", properties.getPageSize())
                    .queryParam("page", currentPage)
                    .queryParam("apiKey", properties.getApiKey())
                    .build())
                .retrieve()
                .body(NewsApiResponse.class);

            if (response == null || response.articles() == null) {
                break;
            }

            totalResults = response.totalResults();

            for (NewsApiResponse.Article article : response.articles()) {
                String sourceName = article.source() != null ? article.source().name() : null;
                allArticles.add(new NewsArticle(
                    sourceName,
                    article.author(),
                    article.title(),
                    article.description()
                ));
            }

            page++;
        } while (allArticles.size() < totalResults);

        return allArticles;
    }
}
