package com.playdata.orderingservice.ordering.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playdata.orderingservice.common.auth.Role;
import com.playdata.orderingservice.common.auth.TokenUserInfo;
import com.playdata.orderingservice.ordering.dto.OrderingSaveReqDto;
import com.playdata.orderingservice.ordering.dto.UserResDto;
import com.playdata.orderingservice.ordering.entity.OrderStatus;
import com.playdata.orderingservice.ordering.entity.Ordering;
import com.playdata.orderingservice.ordering.repository.OrderingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class OrderRetryScheduler {

    private final OrderingRepository orderingRepository;
    private final ObjectMapper objectMapper;
    private final OrderingService orderingService;

    // 특정 작업에 대해 지정된 기간, 시간에 동작하도록 설계하는 spring의 기능.
    // 5분마다 실행
    @Scheduled(fixedDelay = 300_000)
    public void retryPendingOrders() {
        log.info("주문 재처리 스케줄러 시작");

        // DB에서 주문 보류인 엔터티를 전부 조회해서 재처리 대상으로 지정.
        List<Ordering> pendingOrders = orderingRepository.findByOrderStatusIn(List.of(
                OrderStatus.PENDING_USER_FAILURE,
                OrderStatus.PENDING_PROD_NOT_FOUND,
                OrderStatus.PENDING_PROD_STOCK_UPDATE
        ));

        for (Ordering order : pendingOrders) {
            try {
                log.info("재처리 시도 - 주문 ID: {}", order.getId());

                // originalRequestJson → dtoList 복원
                // 주문 재처리를 위해 원본 주문 요청 당시의 상품 내역을 받아오자 (json을 자바 list로)
                List<OrderingSaveReqDto> dtoList = objectMapper.readValue(
                        order.getOriginalRequestJson(),
                        new TypeReference<List<OrderingSaveReqDto>>() {
                        }
                );
                log.info("변환된 dtoList: {}", dtoList);

                // 재처리 (주문 status가 무엇이냐에 따라 분기가 나누어져야 할 것 같아요)
                if (order.getOrderStatus() == OrderStatus.PENDING_USER_FAILURE) {
                    UserResDto userResDto
                            = orderingService.getUserResDto(order.getUserEmail());
                    order.setUserId(userResDto.getId());
                }

                orderingService.processOrderToProductService(dtoList, order.getUserId(), order);

                // 성공했으면 상태 업데이트
                order.updateStatus(OrderStatus.ORDERED);
                orderingRepository.save(order);

                log.info("재처리 성공 - 주문 ID: {}", order.getId());

            } catch (Exception e) {
                log.warn("재처리 실패 - 주문 ID: {}, 이유: {}", order.getId(), e.getMessage());
                e.printStackTrace();
                // 재시도 실패 → 그대로 놔두고 다음번 스케줄링까지 보류
                // 또는 retryCount 등 정책 추가 고려 가능
            }
        }

        log.info("⏹ 주문 재처리 스케줄러 종료");
    }

}
