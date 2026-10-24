package com.financebot.core.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebot.core.dto.telegram_client.SendMessageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@ApplicationScoped
public class TelegramClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final ObjectMapper mapper;
    private final String baseUrl;

    public TelegramClient(ObjectMapper mapper,
                          @ConfigProperty(name = "telegram.api-url") String apiUrl,
                          @ConfigProperty(name = "telegram.bot-token") String botToken) {
        this.mapper = mapper;
        this.baseUrl = apiUrl + botToken;
    }

    public void sendMessage(Long chatId, String text, Object replyMarkup) {
        post("/sendMessage", new SendMessageRequest(chatId, text, replyMarkup));
    }

    public void answerCallback(String callbackQueryId) {
        post("/answerCallbackQuery", Map.of("callback_query_id", callbackQueryId));
    }

    private void post(String path, Object body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2)
                throw new IllegalStateException(
                        "Telegram " + path + " falhou: " + response.statusCode()
                                + " - " + response.body());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Erro ao serializar " + path, e);
        } catch (IOException e) {
            throw new IllegalStateException("Erro de rede em " + path, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Chamada interrompida: " + path, e);
        }
    }
}
