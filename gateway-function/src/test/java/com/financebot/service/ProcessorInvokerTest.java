package com.financebot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebot.core.event.TelegramCommandEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorInvokerTest {

    @Test
    void commandEventIsRegisteredForNativeReflection() throws Exception {
        Class<?> configuration = Class.forName("com.financebot.NativeReflectionConfiguration");
        RegisterForReflection registration = configuration.getAnnotation(RegisterForReflection.class);

        assertNotNull(registration);
        assertTrue(Arrays.asList(registration.targets()).contains(TelegramCommandEvent.class));
        assertTrue(registration.methods());
        assertTrue(registration.fields());
    }

    @Test
    void invokesTransactionWithCompleteCommandPayload() throws Exception {
        AtomicReference<InvokeRequest> captured = new AtomicReference<>();
        LambdaClient client = (LambdaClient) Proxy.newProxyInstance(
                LambdaClient.class.getClassLoader(), new Class<?>[]{LambdaClient.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("invoke") && arguments[0] instanceof InvokeRequest request) {
                        captured.set(request);
                        return InvokeResponse.builder().statusCode(202).build();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        ObjectMapper mapper = new ObjectMapper();
        TelegramCommandEvent event = new TelegramCommandEvent(10L, 20L, "Alice", "/start", null, 30L);

        new ProcessorInvoker(client, mapper, "transaction-function").invoke(event);

        InvokeRequest request = captured.get();
        assertNotNull(request);
        assertEquals("transaction-function", request.functionName());
        assertEquals(InvocationType.EVENT, request.invocationType());
        JsonNode payload = mapper.readTree(request.payload().asUtf8String());
        assertEquals(6, payload.size());
        assertEquals(10L, payload.get("chatId").asLong());
        assertEquals(20L, payload.get("userId").asLong());
        assertEquals("Alice", payload.get("username").asText());
        assertEquals("/start", payload.get("command").asText());
        assertTrue(payload.get("text").isNull());
        assertEquals(30L, payload.get("messageId").asLong());
    }
}
