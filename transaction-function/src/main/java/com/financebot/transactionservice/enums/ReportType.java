package com.financebot.transactionservice.enums;

import com.financebot.transactionservice.exception.BotException;

public enum ReportType {
    EXPENSE, INCOME, ALL;

    public static ReportType fromCallback(String value) {
        try {
            return ReportType.valueOf(value);
        } catch (Exception e) {
            throw new BotException("Tipo de relatório inválido.");
        }
    }
}
