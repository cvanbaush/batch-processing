package com.example.batch_processing.news;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestNewsReaderTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private NewsApiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new NewsApiProperties();
        properties.setBaseUrl("https://newsapi.org");
        properties.setApiKey("test-api-key");
        properties.setPageSize(10);
    }

    @Test
    void read_returnsArticlesOnFirstCall() {
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

        var reader = new RestNewsReader(properties, "nvidia", restClient);
        List<NewsArticle> articles = reader.read();

        assertThat(articles).hasSize(2);
        assertThat(articles.get(0).title()).isEqualTo("Title One");
        assertThat(articles.get(0).source()).isEqualTo("Test Source");
        assertThat(articles.get(0).author()).isEqualTo("Author One");
        assertThat(articles.get(1).title()).isEqualTo("Title Two");
    }

    @Test
    void read_returnsNullOnSecondCall() {
        var response = new NewsApiResponse(
            "ok",
            1,
            List.of(
                new NewsApiResponse.Article(
                    new NewsApiResponse.Source("src1", "Source"),
                    "Author",
                    "Title",
                    "Description",
                    "https://example.com",
                    "2024-01-01T00:00:00Z"
                )
            )
        );
        setupMockResponse(response);

        var reader = new RestNewsReader(properties, "nvidia", restClient);

        assertThat(reader.read()).isNotNull();
        assertThat(reader.read()).isNull();
    }

    @Test
    void read_handlesNullResponse() {
        setupMockResponse(null);

        var reader = new RestNewsReader(properties, "nvidia", restClient);
        List<NewsArticle> articles = reader.read();

        assertThat(articles).isEmpty();
    }

    @Test
    void read_handlesNullArticlesInResponse() {
        var response = new NewsApiResponse("ok", 0, null);
        setupMockResponse(response);

        var reader = new RestNewsReader(properties, "nvidia", restClient);
        List<NewsArticle> articles = reader.read();

        assertThat(articles).isEmpty();
    }

    @Test
    void read_handlesNullSource() {
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

        var reader = new RestNewsReader(properties, "nvidia", restClient);
        List<NewsArticle> articles = reader.read();

        assertThat(articles).hasSize(1);
        assertThat(articles.get(0).source()).isNull();
    }

    @Test
    void read_paginatesThroughAllResults() {
        var page1Response = new NewsApiResponse(
            "ok",
            3,
            List.of(
                new NewsApiResponse.Article(
                    new NewsApiResponse.Source("s1", "Source1"),
                    "Author1", "Title1", "Desc1", "url1", "2024-01-01T00:00:00Z"
                ),
                new NewsApiResponse.Article(
                    new NewsApiResponse.Source("s2", "Source2"),
                    "Author2", "Title2", "Desc2", "url2", "2024-01-01T00:00:00Z"
                )
            )
        );
        var page2Response = new NewsApiResponse(
            "ok",
            3,
            List.of(
                new NewsApiResponse.Article(
                    new NewsApiResponse.Source("s3", "Source3"),
                    "Author3", "Title3", "Desc3", "url3", "2024-01-01T00:00:00Z"
                )
            )
        );

        setupMockResponseSequence(page1Response, page2Response);

        var reader = new RestNewsReader(properties, "nvidia", restClient);
        List<NewsArticle> articles = reader.read();

        assertThat(articles).hasSize(3);
        assertThat(articles.get(0).title()).isEqualTo("Title1");
        assertThat(articles.get(1).title()).isEqualTo("Title2");
        assertThat(articles.get(2).title()).isEqualTo("Title3");
    }

    @Test
    void read_returnsEmptyListWhenNoArticles() {
        var response = new NewsApiResponse("ok", 0, List.of());
        setupMockResponse(response);

        var reader = new RestNewsReader(properties, "nvidia", restClient);
        List<NewsArticle> articles = reader.read();

        assertThat(articles).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void setupMockResponse(NewsApiResponse response) {
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NewsApiResponse.class)).thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private void setupMockResponseSequence(NewsApiResponse first, NewsApiResponse... rest) {
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NewsApiResponse.class)).thenReturn(first, rest);
    }
}
