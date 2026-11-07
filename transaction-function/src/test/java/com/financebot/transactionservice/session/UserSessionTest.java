package com.financebot.transactionservice.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebot.transactionservice.enums.ReportType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {
    @Test
    void sessionSurvivesJsonRoundTripBetweenInvocations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserSession session = new UserSession("/receita", UserSessionStep.WAITING_DATE);
        session.getData().put("amount", new BigDecimal("123.45"));
        session.getData().put("type", ReportType.ALL);
        UserSession restored = mapper.readValue(mapper.writeValueAsString(session), UserSession.class);
        assertEquals(new BigDecimal("123.45"), restored.amount());
        assertEquals(ReportType.ALL, restored.reportType());
        assertEquals(UserSessionStep.WAITING_DATE, restored.getStep());
    }
}
