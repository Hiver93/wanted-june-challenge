package com.kdw.wanted.domain.product.service;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.dto.response.ProductResponseDto;
import com.kdw.wanted.domain.product.repository.ProductRepository;
import com.kdw.wanted.domain.product.repository.ProductTransactionRepository;
import com.kdw.wanted.global.auth.service.JwtService;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.ProductException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductTransactionRepository productTransactionRepository;
	private final JwtService jwtService;
	
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
		Product lastProduct = productRepository.findById(product.getId()).orElseThrow(()->new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
		lastProduct.setName(product.getName());
		lastProduct.setPrice(product.getPrice());
		return lastProduct;
	}

	@Override
	public ProductResponseDto.Detail getProductDetail(Long productId, HttpServletRequest httpRequest) {
		Product product = productRepository.findById(productId).orElseThrow(()->new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
		ProductTransaction productTransaction = null; 
		if(null != httpRequest.getHeader(HttpHeaders.AUTHORIZATION)){
				productTransaction = productTransactionRepository.findByConsumerId(jwtService.getId(httpRequest)).orElse(null);
		}
		return ProductResponseDto.Detail.fromEntity(product, productTransaction);
	}

}
