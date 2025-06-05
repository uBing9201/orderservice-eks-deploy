package com.playdata.orderingservice.ordering.dto;

import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingSaveReqDto {

    // 누가 주문했는지에 대한 정보는 JWT에 있습니다. (프론트가 토큰을 같이 보낼 거에요.)
    private Long productId;
    private int productQuantity;

}









