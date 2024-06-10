package com.kdw.wanted.domain.product.dto.response;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductState;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;
import com.kdw.wanted.domain.product.domain.util.ProductTransactionStateConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.Builder;
import lombok.Data;


public class ProductResponseDto {
	
	@Data
	@Builder
	public static class Element{
		Long id;

		String name;
		
		String providerName;
		
		Long price;
		
		ProductState state;
		
		Long quantity;
		
		Long remaining;
		
		LocalDateTime createdAt;
		
		LocalDateTime updatedAt;
		
		public static ProductResponseDto.Element fromEntity(Product product) {
			return ProductResponseDto.Element.builder()
					.id(product.getId())
					.name(product.getName())
					.providerName(product.getAccount().getUsername())
					.price(product.getPrice())
					.state(product.getState())
					.quantity(product.getQuantity())
					.remaining(product.getRemaning())
					.createdAt(product.getCreatedAt())
					.updatedAt(product.getUpdatedAt())
					.build();
		}
	}
	
	@Data
	@Builder
	public static class Transaction{
		Long id;
		
		Long price;
		
		String state;

		LocalDateTime createdAt;
		
		LocalDateTime updatedAt;
	}
	
	@Data
	@Builder
	public static class Detail{
		
		Long id;

		String name;
		
		Long price;
		
		ProductState state;
		
		Long quantity;
		
		Long remaining;
		
		ProductResponseDto.Transaction transaction;
		
		LocalDateTime createdAt;
		
		LocalDateTime updatedAt;
		public static ProductResponseDto.Detail fromEntity(Product product, ProductTransaction transaction) {
			return ProductResponseDto.Detail.builder()
					.id(product.getId())
					.name(product.getName())
					.price(product.getPrice())
					.state(product.getState())
					.quantity(product.getQuantity())
					.transaction(transaction == null ? null :
								Transaction.builder()
									.id(1L)
									.price(transaction.getPrice())
									.state(transaction.getState().getDesc())
									.createdAt(transaction.getCreatedAt())
									.updatedAt(transaction.getUpdatedAt())
									.build())
					.remaining(product.getRemaning())
					.createdAt(product.getCreatedAt())
					.updatedAt(product.getUpdatedAt())
					.build();
		}
	}
}
