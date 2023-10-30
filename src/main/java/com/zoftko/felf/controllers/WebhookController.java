package com.zoftko.felf.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.zoftko.felf.services.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    value = WebhookController.MAPPING,
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
public class WebhookController {

    public static final String MAPPING = "/gh/hooks";
    public static final String HEADER_EVENT = "X-Github-Event";
    private final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;

    @Autowired
    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<Object> processWebhook(
        @RequestHeader(HEADER_EVENT) String event,
        @RequestBody JsonNode payload
    ) {
        log.info("{} github event received", event);
        webhookService.processEvent(event, payload);

        return ResponseEntity.ok().build();
    }
}
