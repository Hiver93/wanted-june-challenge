package com.kdw.wanted.domain.product.dto.response;

import java.time.LocalDateTime;

import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;

import lombok.Builder;
import lombok.Data;

public class ProductTransactionResponseDto {
	
	@Data
	@Builder
	public static class Element{
		Long id;
		
		Long price;
		
		String state;

		LocalDateTime createdAt;
		
		LocalDateTime updatedAt;
		
		Product product;
		public static ProductTransactionResponseDto.Element fromEntity(ProductTransaction productTransaction) {
			return builder()
					.id(productTransaction.getId())
					.price(productTransaction.getPrice())
					.state(productTransaction.getState().getDesc())
					.createdAt(productTransaction.getCreatedAt())
					.updatedAt(productTransaction.getUpdatedAt())
					.build();
		}
	}
}
