package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.dto.response.ProductResponseDto;
import com.kdw.wanted.domain.product.repository.ProductRepository;
import com.kdw.wanted.domain.product.repository.ProductTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductTransactionRepository productTransactionRepository;
	
	@Override
	public String createProduct(Product product) {
		productRepository.save(product);
		return "success";
	}

	@Override
	public List<Product> getProductList() {
		List<Product> productist = productRepository.findAll();
		return productist;
	}

	@Override
	public Product modifyProduct(Product product) {
		Product lastProduct = productRepository.findById(product.getId()).orElseThrow(()->new RuntimeException("해당 상품 없음"));
		lastProduct.setName(product.getName());
		lastProduct.setPrice(product.getPrice());
		return lastProduct;
	}

	@Override
	public ProductResponseDto.Detail getProductDetail(Long productId, UUID consumerId) {
		Product product = productRepository.findById(productId).orElseThrow(()->new RuntimeException());
		ProductTransaction productTransaction = productTransactionRepository.findByConsumerId(consumerId).orElse(null);
		return ProductResponseDto.Detail.fromEntity(product, productTransaction);
	}

}
