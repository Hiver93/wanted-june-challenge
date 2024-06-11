package com.kdw.wanted.domain.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ProductTransactionRequestDto {
	
	@Data
	public static class Make{
		@NotNull
		Long productId;
		
	}
	
	@Data
	public static class Approve{
		@NotNull
		Long productId;
	}
}
