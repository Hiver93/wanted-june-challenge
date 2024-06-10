package com.kdw.wanted.domain.product.dto.request;

import java.util.UUID;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.Product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ProductReqeustDto {

	@Data
	public static class Regist{
		
		@NotNull
		private String name;
		
		@Min(value = 0)
		private long price;
		
		@Min(value = 0)
		private long quantity;
		
		
		public Product toEntity() {
			return Product.builder()
					.name(this.name)
					.price(this.price)
					.quantity(quantity)
					.remaning(quantity)
					.build();
		}
		
	}
}
