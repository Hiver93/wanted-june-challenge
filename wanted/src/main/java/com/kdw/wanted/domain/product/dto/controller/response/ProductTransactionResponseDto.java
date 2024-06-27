package com.kdw.wanted.domain.product.dto.controller.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.dto.service.response.ProductTransactionServiceResponse;

import lombok.Builder;
import lombok.Data;

public class ProductTransactionResponseDto {
	
	@Data
	@Builder
	public static class Transactions{
		
		List<Element> complete;
		
		List<Element> inProgress;
		
		public static Transactions fromServiceDto(ProductTransactionServiceResponse.Transactions transactions) {
			return Transactions.builder()
									.complete(transactions.getComplete().stream()
														.map(Element::fromEntity)
														.toList())																			
									.inProgress(transactions.getInProgress().stream()
														.map(Element::fromEntity)
														.toList())
									.build();
									
		}
		
		@Data
		@Builder
		public static class Element{
			Long id;
			
			Long price;
			
			String state;

			LocalDateTime createdAt;
			
			LocalDateTime updatedAt;
			
			Long productId;
			
			String productName;
			
			UUID providerId;
			
			String providerName;
			public static Element fromEntity(ProductTransaction productTransaction) {
				return builder()
						.id(productTransaction.getId())
						.price(productTransaction.getPrice())
						.state(productTransaction.getState().getDesc())
						.productId(productTransaction.getProduct().getId())
						.productName(productTransaction.getProduct().getName())
						.providerId(productTransaction.getProduct().getAccount().getId())
						.providerName(productTransaction.getProduct().getAccount().getUsername())
						.createdAt(productTransaction.getCreatedAt())
						.updatedAt(productTransaction.getUpdatedAt())
						.build();
			}
		}
	}
}
