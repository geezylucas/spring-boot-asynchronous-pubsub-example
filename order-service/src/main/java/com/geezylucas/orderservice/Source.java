package com.geezylucas.orderservice;

import com.geezylucas.orderservice.model.UserMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
public class Source {

    private final Sinks.Many<UserMessage> postOffice;

    @Bean
    Supplier<Flux<UserMessage>> generateUserMessages() {
        return postOffice::asFlux;
    }
}
