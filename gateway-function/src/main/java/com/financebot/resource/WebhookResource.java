package com.financebot.resource;

import com.financebot.core.dto.telegram.TelegramUpdateDTO;
import com.financebot.service.TelegramWebhookService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/webhook")
public class WebhookResource {

    private final TelegramWebhookService webhookService;

    public WebhookResource(TelegramWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @POST
    @Path("/telegram")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveEvent(TelegramUpdateDTO update) {
        webhookService.process(update);
        return Response.ok().build();
    }
}
