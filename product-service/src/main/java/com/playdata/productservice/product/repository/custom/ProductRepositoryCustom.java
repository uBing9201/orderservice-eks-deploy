package com.playdata.productservice.product.repository.custom;

import com.playdata.productservice.product.dto.ProductSearchDto;
import com.playdata.productservice.product.entity.Product;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductRepositoryCustom {

    List<Product> productList(ProductSearchDto dto, Pageable pageable);

}
