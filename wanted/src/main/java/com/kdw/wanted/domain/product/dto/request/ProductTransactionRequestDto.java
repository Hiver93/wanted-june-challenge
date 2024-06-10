package com.kdw.wanted.domain.product.dto.request;

import java.util.UUID;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ProductTransactionRequestDto {
	
	@Data
	public static class Make{
		
		@NotNull
		Long productId;
		
		public ProductTransaction toEntity() { 
			return ProductTransaction.builder()
					.product(Product.builder().id(productId).build())
					.build();
		}
		
	}
}
