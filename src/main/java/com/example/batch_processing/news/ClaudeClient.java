package com.example.batch_processing.news;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;

public class ClaudeClient implements AiClient {

    private final AnthropicClient client;

    public ClaudeClient(String apiKey) {
        this.client = AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }

    @Override
    public String summarize(String content) {
        Message message = client.messages().create(
            MessageCreateParams.builder()
                .model("claude-sonnet-4-20250514")
                .maxTokens(1024)
                .addUserMessage("Summarize the following news articles in 2-3 sentences:\n\n" + content)
                .build()
        );

        StringBuilder result = new StringBuilder();
        for (ContentBlock block : message.content()) {
            block.text().ifPresent(result::append);
        }
        return result.toString();
    }
}
