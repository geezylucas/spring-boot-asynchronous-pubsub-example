package com.geezylucas.orderservice;

import com.geezylucas.orderservice.dto.UserMessageDTO;
import com.geezylucas.orderservice.model.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/v1")
public class ConfirmController {

    private final WebClient webClient;
    private final Sinks.Many<UserMessage> postOffice;

    public ConfirmController(WebClient.Builder webClientBuilder,
                             Sinks.Many<UserMessage> postOffice) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8081")
                .build();
        this.postOffice = postOffice;
    }

    @PostMapping("/confirm")
    public Mono<String> sendConfirmFulfillmentCenter(@RequestBody UserMessageDTO userMessageDTO) {
        UserMessage message = new UserMessage(userMessageDTO.getShipmentTrackingId(), userMessageDTO.getUsername(), LocalDateTime.now());

        postOffice.tryEmitNext(message);

        return Mono.just("Confirm sent successfully!");
    }

    @GetMapping("/send-greeting")
    public Mono<String> sendConfirmFulfillmentCenter() {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/greeting")
                        .queryParam("message", "geezylucas")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .log();
    }
}
