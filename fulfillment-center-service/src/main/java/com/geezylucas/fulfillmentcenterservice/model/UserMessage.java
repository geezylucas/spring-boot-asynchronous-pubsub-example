package com.geezylucas.fulfillmentcenterservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserMessage {

    private int shipmentTrackingId;
    private String username;
    private LocalDateTime createdAt;
}
