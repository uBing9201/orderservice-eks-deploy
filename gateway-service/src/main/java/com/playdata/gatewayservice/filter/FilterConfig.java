package com.playdata.gatewayservice.filter;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

// 게이트웨이 라우팅 및 필터 등록 방식은 크게 2가지로 나뉩니다.
// 클래스 빈 등록 방식, yml에 등록하는 방식
public class FilterConfig {

//    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/first-service/**")
                        .filters(f -> f.addRequestHeader("first-request", "first-request-header"))
                        .uri("http://localhost:8181"))

                .route(r -> r.path("/second-service/**")
                        .filters(f -> f.addRequestHeader("second-request", "second-request-header"))
                        .uri("http://localhost:8282"))

                .build();
    }

}
