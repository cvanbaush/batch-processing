package com.example.batch_processing.news;

import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestClient;

public class OpenAiClient implements AiClient {

    private final RestClient restClient;

    public OpenAiClient(String apiKey) {
        this.restClient = RestClient.builder()
            .baseUrl("https://api.openai.com")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public String summarize(String content) {
        Map<String, Object> request = Map.of(
            "model", "gpt-4o-mini",
            "messages", List.of(
                Map.of("role", "user", "content",
                    "Summarize the following news articles in 2-3 sentences:\n\n" + content)
            ),
            "max_tokens", 1024
        );

        OpenAiResponse response = restClient.post()
            .uri("/v1/chat/completions")
            .body(request)
            .retrieve()
            .body(OpenAiResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return "";
        }

        return response.choices().get(0).message().content();
    }

    public record OpenAiResponse(List<Choice> choices) {}
    public record Choice(Message message) {}
    public record Message(String role, String content) {}
}
