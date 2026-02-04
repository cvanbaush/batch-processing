package com.example.batch_processing.news;

import org.springframework.batch.infrastructure.item.ItemReader;

import java.util.List;

public class RestNewsReader implements ItemReader<List<NewsArticle>> {

    private final NewsClient newsClient;
    private final String keyword;

    private boolean read = false;

    public RestNewsReader(NewsClient newsClient, String keyword) {
        this.newsClient = newsClient;
        this.keyword = keyword;
    }

    @Override
    public List<NewsArticle> read() {
        if (read) {
            return null;
        }
        read = true;
        return newsClient.fetchArticles(keyword);
    }
}
