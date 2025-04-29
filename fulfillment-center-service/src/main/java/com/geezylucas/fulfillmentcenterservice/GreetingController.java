package com.geezylucas.fulfillmentcenterservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class GreetingController {

    @GetMapping("/greeting")
    public Mono<String> greeting(@RequestParam String message) {
        log.info("greeting: {}", message);
        return Mono.just(String.format("Hello %s!", message));
    }
}
