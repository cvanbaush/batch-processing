package com.example.batch_processing.client;

import java.util.List;

import com.example.batch_processing.model.NewsArticle;

public interface NewsClient {
    List<NewsArticle> fetchArticles(String keyword);
}
