package com.example.batch_processing.news;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Component
@ConfigurationProperties(prefix = "newsapi")
public class NewsApiProperties {

    private String baseUrl;
    private String apiKey;
    private int pageSize;

    // getters + setters
}

