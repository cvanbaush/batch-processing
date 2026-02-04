package com.example.batch_processing.news;

import tools.jackson.databind.ObjectMapper;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

public class JsonLinesNewsWriter implements ItemWriter<List<NewsArticle>> {

    private final Path outputPath;
    private final ObjectMapper objectMapper;

    public JsonLinesNewsWriter(Path outputPath) {
        this.outputPath = outputPath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void write(Chunk<? extends List<NewsArticle>> chunk) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
            for (List<NewsArticle> articleList : chunk) {
                for (NewsArticle article : articleList) {
                    writer.write(objectMapper.writeValueAsString(article));
                    writer.newLine();
                }
            }
        }
    }
}
