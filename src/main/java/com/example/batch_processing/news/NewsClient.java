package com.example.batch_processing.news;

import java.util.List;

public interface NewsClient {
    List<NewsArticle> fetchArticles(String keyword);
}
