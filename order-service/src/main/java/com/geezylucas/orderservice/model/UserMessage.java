package com.geezylucas.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserMessage {

    private int shipmentTrackingId;
    private String username;
    private LocalDateTime createdAt;
}
