package com.playdata.orderingservice.ordering.entity;

public enum OrderStatus {
    ORDERED, // 주문 완료
    PENDING_USER_FAILURE, // 주문 보류 (user-service의 장애)
    PENDING_PROD_NOT_FOUND, // 주문 보류 (product-service의 상품 조회 장애)
    PENDING_PROD_STOCK_UPDATE, // 주문 보류 (product-service의 재고 감소 장애)
    CANCELED // 주문 취소
}
