package com.example.batch_processing.batch;

import org.springframework.batch.infrastructure.item.ItemReader;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.example.batch_processing.model.NewsArticle;

public class JsonLinesNewsReader implements ItemReader<List<NewsArticle>> {

    private final Path inputPath;
    private final ObjectMapper objectMapper;

    private boolean read = false;

    public JsonLinesNewsReader(Path inputPath) {
        this.inputPath = inputPath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<NewsArticle> read() throws Exception {
        if (read) {
            return null;
        }
        read = true;

        List<NewsArticle> articles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                articles.add(objectMapper.readValue(line, NewsArticle.class));
            }
        }
        return articles;
    }
}
