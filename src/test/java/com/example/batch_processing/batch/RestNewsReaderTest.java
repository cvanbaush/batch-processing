package com.example.batch_processing.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import com.example.batch_processing.client.NewsClient;
import com.example.batch_processing.model.NewsArticle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestNewsReaderTest {

    @Mock
    private NewsClient newsClient;

    @Test
    void read_returnsArticlesOnFirstCall() {
        var articles = List.of(
            new NewsArticle("Test Source", "Author One", "Title One", "Description One"),
            new NewsArticle("Another Source", "Author Two", "Title Two", "Description Two")
        );
        when(newsClient.fetchArticles("nvidia")).thenReturn(articles);

        var reader = new RestNewsReader(newsClient, "nvidia");
        List<NewsArticle> result = reader.read();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Title One");
        assertThat(result.get(0).source()).isEqualTo("Test Source");
        assertThat(result.get(0).author()).isEqualTo("Author One");
        assertThat(result.get(1).title()).isEqualTo("Title Two");
    }

    @Test
    void read_returnsNullOnSecondCall() {
        when(newsClient.fetchArticles("nvidia")).thenReturn(List.of(
            new NewsArticle("Source", "Author", "Title", "Description")
        ));

        var reader = new RestNewsReader(newsClient, "nvidia");

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
    }

    @Test
    void read_returnsEmptyListWhenNoArticles() {
        when(newsClient.fetchArticles("nvidia")).thenReturn(List.of());

        var reader = new RestNewsReader(newsClient, "nvidia");
        List<NewsArticle> result = reader.read();

        assertThat(result).isEmpty();
    }
}
