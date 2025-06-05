package com.playdata.orderingservice.ordering.controller;

import com.playdata.orderingservice.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CircuitDemoController {

    private final UserServiceClient userServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    // 1. 서킷브레이커 없는 버전
    @GetMapping("/demo/no-circuit")
    public ResponseEntity<List<String>> demoWithoutCircuit() {
        List<String> results = new ArrayList<>();

        // 10번 연속 호출
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            try {
                log.info("호출 {}번째 시도 (서킷브레이커 없음)", i+1);

                userServiceClient.findByEmail("test@test.com");
                results.add("요청 " + (i+1) + ": 성공 (" + (System.currentTimeMillis() - start) + "ms)");
            } catch (Exception e) {
                results.add("요청 " + (i+1) + ": 실패 (" + (System.currentTimeMillis() - start) + "ms) - " + e.getMessage());
            }
        }

        return ResponseEntity.ok(results);
    }

    // 2. 서킷브레이커 있는 버전
    @GetMapping("/demo/with-circuit")
    public ResponseEntity<List<String>> demoWithCircuit() {
        List<String> results = new ArrayList<>();
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        // 10번 연속 호출
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            try {
                log.info("호출 {}번째 시도 (서킷브레이커 적용)", i+1);

                String result = circuitBreaker.run(
                        () -> {
                            userServiceClient.findByEmail("test@test.com");
                            return "성공 (" + (System.currentTimeMillis() - start) + "ms)";
                        },
                        throwable -> {
                            if (throwable instanceof CallNotPermittedException) {
                                // 서킷 오픈됨
                                return "서킷 OPEN! 즉시 대체 응답 (" + (System.currentTimeMillis() - start) + "ms)";
                            }
                            return "실패 (" + (System.currentTimeMillis() - start) + "ms) - " + throwable.getMessage();
                        }
                );

                results.add("요청 " + (i+1) + ": " + result);
            } catch (Exception e) {
                results.add("요청 " + (i+1) + ": 예외 발생 - " + e.getMessage());
            }


        }

        return ResponseEntity.ok(results);
    }


}









