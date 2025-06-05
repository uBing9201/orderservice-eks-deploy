package com.playdata.productservice.product.repository;

import com.playdata.productservice.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 검색 조건(카테고리, 검색어)에 따른 페이징
    @Query("SELECT p FROM Product p WHERE p.category LIKE %?1%")
    Page<Product> findByCategoryValue(String keyword,
                                      Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1%")
    Page<Product> findByNameValue(String keyword,
                                  Pageable pageable);

    List<Product> findByIdIn(List<Long> ids);

}
