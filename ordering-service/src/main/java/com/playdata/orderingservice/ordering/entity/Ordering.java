package com.playdata.orderingservice.ordering.entity;

import com.playdata.orderingservice.ordering.dto.OrderingListResDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
public class Ordering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    */

    // 프로젝트가 나눠지면서 Ordering 쪽에서는 User 엔터티에 대한 정보를 확인할 수 없다.
    // 클라이언트 단에서 넘어오는 정보만 저장할 수 있다.
    @JoinColumn
    @Setter
    private Long userId;

//    MSA에선 도메인별로 분리돼 있어서 FK처럼 다른 서비스의 ID를 바로 못 가져올 수도 있음.
//    그러니까 서비스 간 통신 실패 상황을 대비해서 최소 식별자(email 같은 거)는
//    자기 도메인 안에 보관하는 게 정석이다. (나중에 재처리에서 사용하기 위해 설정)
    private String userEmail;

    /*
    주문 과정에서 장애가 발생할 경우, 일단 주문을 보류 시켜놓고 나중에 재처리 한다고 했습니다.
    재처리를 할 때 원본 주문 내역이 무엇인지를 알아야 하잖아요.
    엔터티는 주문 내역을 List<OrderDetail>로 관리하고 있는데, DB에는 저 형태로 저장이 불가능.
    그래서 리스트 안에 객체가 있다는 형태를 JSON 문자열로 변환해서 DB에 저장을 하기 위한 필드를 추가.
     */
    @Column(columnDefinition = "TEXT") // TEXT 타입은 일반적인 VARCHAR보다 더 큰 문자열을 저장할 수 있는 타입
    @Setter
    private String originalRequestJson;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    // CascadeType.PERSIST로 설정하면 새로운 엔터티 생성만 처리하고 기존 엔터티 업데이트는
    // 자동으로 처리되지 않습니다. -> MERGE (부모 엔터티 업데이트 시 연관 엔터티도 함께 업데이트)
    @OneToMany(mappedBy = "ordering", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    // lombok의 setter를 사용해도 되지만, 원하는 필드값을 수정하기 위한 메서드를 직접 작성해도 됩니다.
    // 조건문, 반복문 등을 세팅해야 된다면 더더욱 직접 setter를 만들어야 합니다.
    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }


    // dto 변환 메서드
    public OrderingListResDto fromEntity(
            String email, Map<Long, String> productIdToNameMap
    ) {
        List<OrderDetail> orderDetailList = this.getOrderDetails();
        List<OrderingListResDto.OrderDetailDto> orderDetailDtos
                = new ArrayList<>();

        // OrderDetail 엔터티를 OrderDetailDto로 변환해야 합니다.
        for (OrderDetail orderDetail : orderDetailList) {
            OrderingListResDto.OrderDetailDto orderDetailDto
                    = orderDetail.fromEntity(productIdToNameMap);
            orderDetailDtos.add(orderDetailDto);
        }


        return OrderingListResDto.builder()
                .id(id)
                .userEmail(email)
                .orderStatus(orderStatus)
                .orderDetails(orderDetailDtos)
                .build();
    }

}










