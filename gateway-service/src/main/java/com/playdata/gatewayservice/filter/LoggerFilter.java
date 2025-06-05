package com.playdata.gatewayservice.filter;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggerFilter
        extends AbstractGatewayFilterFactory<LoggerFilter.Config> {

    // Filter가 빈으로 등록될 때 부모 클래스의 생성자에게
    // 이미 정적으로 세팅된 특정 설정값을 전달합니다.
    public LoggerFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // 필터의 우선순위를 적용하고 싶다면 OrderedGatewayFilter를 직접 생성한다.
        // 첫번째 매개값은 익명 객체 선언, 두번째 매개값으로 우선순위 상수를 넘깁니다.
        // 만약 여러 필터에 순위를 매기고 싶다면 정수로 지정합니다. (낮은 숫자일수록 우선순위 높음)
        return new OrderedGatewayFilter((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Logger Filter active! base Message = {}", config.getBaseMessage());
            if (config.isPreLogger()) {
                log.info("Request URI: {}",  request.getURI());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("Logger Post Filter active! response code = {}", response.getStatusCode());
                }
            }));

        }, Ordered.HIGHEST_PRECEDENCE);
    }

    @Getter @Setter @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        // 필터 동작을 동적으로 변경하거나 설정하기 위해 사용합니다. (선택 사항)
        // 필터 동작을 YAML 설정 파일을 통해 외부에서 동적으로 설정할 수 있습니다.
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }


}





