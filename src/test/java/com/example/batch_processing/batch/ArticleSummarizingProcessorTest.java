package com.example.batch_processing.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import com.example.batch_processing.client.AiClient;
import com.example.batch_processing.model.NewsArticle;
import com.example.batch_processing.model.NewsSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleSummarizingProcessorTest {

    @Mock
    private AiClient aiClient;

    @Test
    void process_summarizesArticles() {
        var articles = List.of(
            new NewsArticle("TechNews", "John Doe", "AI Advances", "New breakthroughs in AI"),
            new NewsArticle("SciDaily", "Jane Smith", "Quantum Computing", "Quantum computers reach milestone")
        );
        when(aiClient.summarize(anyString())).thenReturn("AI and quantum computing are advancing rapidly.");

        var processor = new ArticleSummarizingProcessor(aiClient, "technology");
        NewsSummary result = processor.process(articles);

        assertThat(result.keyword()).isEqualTo("technology");
        assertThat(result.summary()).isEqualTo("AI and quantum computing are advancing rapidly.");
        assertThat(result.processedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void process_passesFormattedArticlesToAiClient() {
        var articles = List.of(
            new NewsArticle("Source1", "Author1", "Title One", "Description One"),
            new NewsArticle("Source2", "Author2", "Title Two", "Description Two")
        );
        when(aiClient.summarize(anyString())).thenReturn("Summary");

        var processor = new ArticleSummarizingProcessor(aiClient, "test");
        processor.process(articles);

        String expectedText = "Title: Title One\nDescription: Description One\n\nTitle: Title Two\nDescription: Description Two";
        verify(aiClient).summarize(expectedText);
    }

    @Test
    void process_returnsNoArticlesSummaryWhenListIsEmpty() {
        var processor = new ArticleSummarizingProcessor(aiClient, "empty");
        NewsSummary result = processor.process(List.of());

        assertThat(result.keyword()).isEqualTo("empty");
        assertThat(result.summary()).isEqualTo("no articles");
        assertThat(result.processedDate()).isEqualTo(LocalDate.now());
        verify(aiClient, never()).summarize(anyString());
    }

    @Test
    void process_returnsNoArticlesSummaryWhenNull() {
        var processor = new ArticleSummarizingProcessor(aiClient, "null");
        NewsSummary result = processor.process(null);

        assertThat(result.keyword()).isEqualTo("null");
        assertThat(result.summary()).isEqualTo("no articles");
        verify(aiClient, never()).summarize(anyString());
    }
}
