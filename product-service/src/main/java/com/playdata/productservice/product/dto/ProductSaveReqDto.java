package com.playdata.productservice.product.dto;

import com.playdata.productservice.product.entity.Product;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSaveReqDto {

    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private MultipartFile productImage;

    public Product toEntity() {
        return Product.builder()
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .build();
    }

}








