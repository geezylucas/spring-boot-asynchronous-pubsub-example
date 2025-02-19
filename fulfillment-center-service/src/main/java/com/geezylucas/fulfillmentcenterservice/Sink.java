package com.geezylucas.fulfillmentcenterservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geezylucas.fulfillmentcenterservice.model.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.context.config.JsonMessageConverter;
import org.springframework.cloud.function.json.JacksonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class Sink {

    @Bean
    public Consumer<UserMessage> logUserMessage() {
        return userMessage -> log.info("New shipment tracking id received {}, username {} at {}", userMessage.getShipmentTrackingId(), userMessage.getUsername(), userMessage.getCreatedAt());
    }

    // This is workaround for https://github.com/GoogleCloudPlatform/spring-cloud-gcp/issues/3139
    // and should be removed once fix https://github.com/spring-cloud/spring-cloud-function/pull/1162
    // is deployed
    @Bean
    public JsonMessageConverter customJsonMessageConverter(ObjectMapper objectMapper) {
        return new JsonMessageConverter(new JacksonMapper(objectMapper));
    }
}
