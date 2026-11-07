package com.financebot.transactionservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.financebot.core.event.TelegramCommandEvent;
import com.financebot.transactionservice.service.FunctionService;
import jakarta.inject.Named;

@Named("transaction")
public class TransactionFunction implements RequestHandler<TelegramCommandEvent, Void> {
    private final FunctionService functionService;

    public TransactionFunction(FunctionService functionService) {
        this.functionService = functionService;
    }

    @Override
    public Void handleRequest(TelegramCommandEvent event, Context context) {
        functionService.process(event);
        return null;
    }
}
