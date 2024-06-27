package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.dto.controller.response.ProductResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface ProductService {
	public String createProduct(Product product);
	
	public Product getProduct(Long productId);
	
	public List<Product> getProductList();
	
	public Product modifyProduct(Product product);
}
