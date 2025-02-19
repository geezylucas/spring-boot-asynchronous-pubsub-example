package com.geezylucas.orderservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserMessageDTO {

    private int shipmentTrackingId;
    private String username;
}
