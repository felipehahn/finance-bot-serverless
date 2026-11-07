package com.financebot.transactionservice.session;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    public java.math.BigDecimal amount() {
        return new java.math.BigDecimal(data.get("amount").toString());
    }

    public com.financebot.transactionservice.enums.ReportType reportType() {
        return com.financebot.transactionservice.enums.ReportType.valueOf(data.get("type").toString());
    }

    private String command;
    private UserSessionStep step;
    private Map<String, Object> data = new HashMap<>();

    public UserSession(String command, UserSessionStep step) {
        this.command = command;
        this.step = step;
        this.data = new HashMap<String, Object>();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String value) {
        this.command = value;
    }

    public UserSessionStep getStep() {
        return step;
    }

    public void setStep(UserSessionStep value) {
        this.step = value;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> value) {
        this.data = value;
    }

    public UserSession() {
    }
}
