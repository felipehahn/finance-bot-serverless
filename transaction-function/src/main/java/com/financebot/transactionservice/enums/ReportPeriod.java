package com.financebot.transactionservice.enums;

import com.financebot.transactionservice.exception.BotException;

public enum ReportPeriod {
    CURRENT, CUSTOM;

    public static ReportPeriod fromCallback(String value) {
        try {
            return ReportPeriod.valueOf(value);
        } catch (Exception e) {
            throw new BotException("Período de relatório inválido.");
        }
    }
}
