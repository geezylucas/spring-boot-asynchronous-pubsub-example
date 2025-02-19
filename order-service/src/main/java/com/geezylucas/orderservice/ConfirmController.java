package com.geezylucas.orderservice;

import com.geezylucas.orderservice.dto.UserMessageDTO;
import com.geezylucas.orderservice.model.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ConfirmController {

    private final Sinks.Many<UserMessage> postOffice;

    @PostMapping("/confirm")
    public Mono<String> sendConfirmFulfillmentCenter(@RequestBody UserMessageDTO userMessageDTO) {
        UserMessage message = new UserMessage(userMessageDTO.getShipmentTrackingId(), userMessageDTO.getUsername(), LocalDateTime.now());

        postOffice.tryEmitNext(message);

        return Mono.just("Confirm sent successfully!");
    }
}
