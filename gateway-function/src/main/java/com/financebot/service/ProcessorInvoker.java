package com.financebot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebot.core.event.TelegramCommandEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import java.io.UncheckedIOException;

@ApplicationScoped
public class ProcessorInvoker {

    private final LambdaClient lambda;
    private final ObjectMapper mapper;
    private final String functionName;

    public ProcessorInvoker(LambdaClient lambda,
                            ObjectMapper mapper,
                            @ConfigProperty(name = "processor.function-name") String functionName) {
        this.lambda = lambda;
        this.mapper = mapper;
        this.functionName = functionName;
    }

    public void invoke(TelegramCommandEvent event) {
        try {
            lambda.invoke(InvokeRequest.builder()
                    .functionName(functionName)
                    .invocationType(InvocationType.EVENT)
                    .payload(SdkBytes.fromUtf8String(mapper.writeValueAsString(event)))
                    .build());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
