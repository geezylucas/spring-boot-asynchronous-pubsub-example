package com.geezylucas.orderservice;

import com.geezylucas.orderservice.model.UserMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Sinks;

@SpringBootApplication
public class OrderServiceApplication {

	@Bean
	public Sinks.Many<UserMessage> postOffice() {
		return Sinks.many().unicast().onBackpressureBuffer();
	}

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
