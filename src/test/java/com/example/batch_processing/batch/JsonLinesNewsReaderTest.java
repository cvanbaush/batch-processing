package com.example.batch_processing.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.example.batch_processing.model.NewsArticle;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLinesNewsReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void read_parsesJsonLinesFile() throws Exception {
        Path inputPath = tempDir.resolve("articles.jsonl");
        Files.writeString(inputPath, """
            {"source":"TechNews","author":"John Doe","title":"Breaking News","description":"Something happened"}
            {"source":"SciDaily","author":"Jane Smith","title":"Discovery","description":"Scientists found something"}
            """);

        var reader = new JsonLinesNewsReader(inputPath);
        List<NewsArticle> result = reader.read();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).source()).isEqualTo("TechNews");
        assertThat(result.get(0).author()).isEqualTo("John Doe");
        assertThat(result.get(0).title()).isEqualTo("Breaking News");
        assertThat(result.get(0).description()).isEqualTo("Something happened");
        assertThat(result.get(1).source()).isEqualTo("SciDaily");
        assertThat(result.get(1).title()).isEqualTo("Discovery");
    }

    @Test
    void read_returnsNullOnSecondCall() throws Exception {
        Path inputPath = tempDir.resolve("articles.jsonl");
        Files.writeString(inputPath, """
            {"source":"Source","author":"Author","title":"Title","description":"Description"}
            """);

        var reader = new JsonLinesNewsReader(inputPath);

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
    }

    @Test
    void read_handlesEmptyFile() throws Exception {
        Path inputPath = tempDir.resolve("empty.jsonl");
        Files.writeString(inputPath, "");

        var reader = new JsonLinesNewsReader(inputPath);
        List<NewsArticle> result = reader.read();

        assertThat(result).isEmpty();
    }

    @Test
    void read_handlesSpecialCharacters() throws Exception {
        Path inputPath = tempDir.resolve("special.jsonl");
        Files.writeString(inputPath, """
            {"source":"Source","author":"Author","title":"Title with \\"quotes\\"","description":"Line1\\nLine2"}
            """);

        var reader = new JsonLinesNewsReader(inputPath);
        List<NewsArticle> result = reader.read();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Title with \"quotes\"");
        assertThat(result.get(0).description()).isEqualTo("Line1\nLine2");
    }

    @Test
    void read_handlesNullFields() throws Exception {
        Path inputPath = tempDir.resolve("nulls.jsonl");
        Files.writeString(inputPath, """
            {"source":"Source","author":null,"title":"Title","description":null}
            """);

        var reader = new JsonLinesNewsReader(inputPath);
        List<NewsArticle> result = reader.read();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).source()).isEqualTo("Source");
        assertThat(result.get(0).author()).isNull();
        assertThat(result.get(0).title()).isEqualTo("Title");
        assertThat(result.get(0).description()).isNull();
    }
}
