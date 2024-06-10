package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.dto.response.ProductResponseDto;

public interface ProductService {
	public String createProduct(Product product);
	
	public List<Product> getProductList();
	
	public Product modifyProduct(Product product);
	
	public ProductResponseDto.Detail getProductDetail(Long productId, UUID consumerId);
}
