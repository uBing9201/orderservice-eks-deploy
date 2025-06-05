package com.playdata.productservice.product.entity;

import com.playdata.productservice.common.entity.BaseTimeEntity;
import com.playdata.productservice.product.dto.ProductResDto;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_product")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private int price;
    @Setter
    private int stockQuantity;
    @Setter // 이미지 경로를 위해서만 setter 세팅
    private String imagePath;

    public ProductResDto fromEntity() {
        return ProductResDto.builder()
                .id(id)
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .imagePath(imagePath)
                .build();
    }

}







