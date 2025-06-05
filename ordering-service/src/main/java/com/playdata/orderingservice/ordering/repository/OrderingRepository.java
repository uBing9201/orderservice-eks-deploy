package com.playdata.orderingservice.ordering.repository;

import com.playdata.orderingservice.ordering.entity.OrderStatus;
import com.playdata.orderingservice.ordering.entity.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderingRepository extends JpaRepository<Ordering, Long> {

    //    @Query("SELECT o FROM Ordering o WHERE o.userId = ?1")
    List<Ordering> findByUserId(Long userId);

    // 쿼리메서드: list로 전달된 status 값 중에 하나라도 포함되어 있다면 조회 대상에 포함.
    List<Ordering> findByOrderStatusIn(List<OrderStatus> statuses);


}
