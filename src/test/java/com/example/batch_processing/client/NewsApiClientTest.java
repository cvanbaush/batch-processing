package com.example.batch_processing.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;

import com.example.batch_processing.config.NewsApiProperties;
import com.example.batch_processing.model.NewsApiResponse;
import com.example.batch_processing.model.NewsArticle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsApiClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private NewsApiProperties properties;
    private NewsApiClient newsApiClient;

    @BeforeEach
    void setUp() {
        properties = new NewsApiProperties();
        properties.setBaseUrl("https://newsapi.org");
        properties.setApiKey("test-api-key");
        properties.setPageSize(10);
        newsApiClient = new NewsApiClient(properties, restClient);
    }

    @Test
    void fetchArticles_returnsArticles() {
        var response = new NewsApiResponse(
            "ok",
            2,
            List.of(
                new NewsApiResponse.Article(
                    new NewsApiResponse.Source("src1", "Test Source"),
                    "Author One",
                    "Title One",
                    "Description One",
                    "https://example.com/1",
                    "2024-01-01T00:00:00Z"
                ),
                new NewsApiResponse.Article(
                    new NewsApiResponse.Source("src2", "Another Source"),
                    "Author Two",
                    "Title Two",
                    "Description Two",
                    "https://example.com/2",
                    "2024-01-02T00:00:00Z"
                )
            )
        );
        setupMockResponse(response);

        List<NewsArticle> articles = newsApiClient.fetchArticles("nvidia");

        assertThat(articles).hasSize(2);
        assertThat(articles.get(0).title()).isEqualTo("Title One");
        assertThat(articles.get(0).source()).isEqualTo("Test Source");
        assertThat(articles.get(0).author()).isEqualTo("Author One");
        assertThat(articles.get(0).description()).isEqualTo("Description One");
        assertThat(articles.get(1).title()).isEqualTo("Title Two");
    }

    @Test
    void fetchArticles_handlesNullResponse() {
        setupMockResponse(null);

        List<NewsArticle> articles = newsApiClient.fetchArticles("nvidia");

        assertThat(articles).isEmpty();
    }

    @Test
    void fetchArticles_handlesNullArticlesInResponse() {
        var response = new NewsApiResponse("ok", 0, null);
        setupMockResponse(response);

        List<NewsArticle> articles = newsApiClient.fetchArticles("nvidia");

        assertThat(articles).isEmpty();
    }

    @Test
    void fetchArticles_handlesNullSource() {
        var response = new NewsApiResponse(
            "ok",
            1,
            List.of(
                new NewsApiResponse.Article(
                    null,
                    "Author",
                    "Title",
                    "Description",
                    "https://example.com",
                    "2024-01-01T00:00:00Z"
                )
            )
        );
        setupMockResponse(response);

        List<NewsArticle> articles = newsApiClient.fetchArticles("nvidia");

        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).source()).isNull();
        assertThat(articles.get(0).title()).isEqualTo("Title");
    }

    @Test
    void fetchArticles_returnsEmptyListWhenNoArticles() {
        var response = new NewsApiResponse("ok", 0, List.of());
        setupMockResponse(response);

        List<NewsArticle> articles = newsApiClient.fetchArticles("nvidia");

        assertThat(articles).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void setupMockResponse(NewsApiResponse response) {
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NewsApiResponse.class)).thenReturn(response);
    }
}
