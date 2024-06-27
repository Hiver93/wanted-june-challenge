package com.kdw.wanted.domain.product.dto.service.response;

import java.util.ArrayList;
import java.util.List;

import com.kdw.wanted.domain.product.domain.ProductTransaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ProductTransactionServiceResponse {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Transactions {
		List<ProductTransaction> complete = new ArrayList<>();
		List<ProductTransaction> inProgress = new ArrayList<>();
	}
}
