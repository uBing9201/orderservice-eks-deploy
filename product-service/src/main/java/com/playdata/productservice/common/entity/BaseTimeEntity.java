package com.playdata.productservice.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 테이블과 관련이 없고, 컬럼 정보만 자식에게 제공하기 위해서 사용하는 아노테이션.
// 직접 사용되지 않고 반드시 상속을 통해 구현되어야 한다는 것을 강조하기 위해 abstract를 붙입니다.
public abstract class BaseTimeEntity {

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

}
