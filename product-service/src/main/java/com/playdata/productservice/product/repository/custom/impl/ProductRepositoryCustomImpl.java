package com.playdata.productservice.product.repository.custom.impl;

import static com.playdata.productservice.product.entity.QProduct.product;

import com.playdata.productservice.product.dto.ProductSearchDto;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.product.repository.custom.ProductRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.BooleanBuilder;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> productList(ProductSearchDto dto, Pageable pageable) {

        return queryFactory
                .selectFrom(product)
                .where(buildCondition(dto))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanBuilder buildCondition(ProductSearchDto dto) {
        BooleanBuilder builder = new BooleanBuilder();

        if (dto.getCategory() != null) {
            switch (dto.getCategory()) {
                case "name":
                    builder.and(product.name.contains(dto.getSearchName()));
                    break;
                case "category":
                    builder.and(product.category.contains(dto.getSearchName()));
                    break;
            }
        }

//        if(dto.getCategory() != null) {
//            if(dto.getCategory().equals("name")) {
//                builder.and(product.name.contains(dto.getSearchName()));
//            }
//            if(dto.getCategory().equals("category")) {
//                builder.and(product.category.contains(dto.getSearchName()));
//            }
//        }

        return builder;
    }
}
