package com.kdw.wanted.domain.product.dto.controller.request;

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
		Long productTransactionId;
	}
	
	@Data
	public static class Confirm{
		@NotNull
		Long productTransactionId;
	}
}
