package com.financebot;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
@QuarkusTest
class WebhookResourceTest {
    @Test
    void shouldRejectRequestWithoutTelegramSecret() {
        given()
          .contentType("application/json")
          .when().post("/webhook/telegram")
          .then()
             .statusCode(401);
    }
}
