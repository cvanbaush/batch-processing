package com.example.batch_processing.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.infrastructure.item.Chunk;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.example.batch_processing.model.NewsArticle;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLinesNewsWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void write_createsJsonLinesFile() throws Exception {
        Path outputPath = tempDir.resolve("articles.jsonl");
        var writer = new JsonLinesNewsWriter(outputPath);

        var articles = List.of(
            new NewsArticle("TechNews", "John Doe", "Breaking News", "Something happened"),
            new NewsArticle("SciDaily", "Jane Smith", "Discovery", "Scientists found something")
        );

        writer.write(new Chunk<>(List.of(articles)));

        assertThat(outputPath).exists();
        List<String> lines = Files.readAllLines(outputPath);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("\"source\":\"TechNews\"");
        assertThat(lines.get(0)).contains("\"title\":\"Breaking News\"");
        assertThat(lines.get(1)).contains("\"source\":\"SciDaily\"");
        assertThat(lines.get(1)).contains("\"title\":\"Discovery\"");
    }

    @Test
    void write_createsParentDirectories() throws Exception {
        Path outputPath = tempDir.resolve("nested/dir/articles.jsonl");
        var writer = new JsonLinesNewsWriter(outputPath);

        var articles = List.of(
            new NewsArticle("Source", "Author", "Title", "Description")
        );

        writer.write(new Chunk<>(List.of(articles)));

        assertThat(outputPath).exists();
        assertThat(outputPath.getParent()).exists();
    }

    @Test
    void write_handlesEmptyArticleList() throws Exception {
        Path outputPath = tempDir.resolve("empty.jsonl");
        var writer = new JsonLinesNewsWriter(outputPath);

        writer.write(new Chunk<>(List.of(List.of())));

        assertThat(outputPath).exists();
        assertThat(Files.readAllLines(outputPath)).isEmpty();
    }

    @Test
    void write_handlesMultipleChunks() throws Exception {
        Path outputPath = tempDir.resolve("multi.jsonl");
        var writer = new JsonLinesNewsWriter(outputPath);

        var chunk1 = List.of(new NewsArticle("S1", "A1", "T1", "D1"));
        var chunk2 = List.of(new NewsArticle("S2", "A2", "T2", "D2"));

        writer.write(new Chunk<>(List.of(chunk1, chunk2)));

        List<String> lines = Files.readAllLines(outputPath);
        assertThat(lines).hasSize(2);
    }

    @Test
    void write_escapesSpecialCharactersInJson() throws Exception {
        Path outputPath = tempDir.resolve("special.jsonl");
        var writer = new JsonLinesNewsWriter(outputPath);

        var articles = List.of(
            new NewsArticle("Source", "Author", "Title with \"quotes\"", "Description with\nnewline")
        );

        writer.write(new Chunk<>(List.of(articles)));

        String content = Files.readString(outputPath);
        assertThat(content).contains("\\\"quotes\\\"");
        assertThat(content).contains("\\n");
    }
}
