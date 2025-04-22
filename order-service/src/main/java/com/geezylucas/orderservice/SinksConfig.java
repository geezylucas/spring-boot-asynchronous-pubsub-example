package com.geezylucas.orderservice;

import com.geezylucas.orderservice.model.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinksConfig {

    @Bean
    public Sinks.Many<UserMessage> postOffice() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }
}
