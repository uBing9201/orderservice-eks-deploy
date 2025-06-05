package com.playdata.productservice.product.dto;

import lombok.*;

@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchDto {

    private String category;
    private String searchName;

}
