package com.kdw.wanted.domain.product.service;

import java.util.UUID;

import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.dto.service.response.ProductTransactionServiceResponse;

public interface ProductTransactionService {
	public String makeTransaction(Long productId, UUID consumerID);
	
	public ProductTransactionServiceResponse.Transactions getTransactions(UUID accountId);
	
	public String approveTransaction(Long productTransactionId, UUID providerId);
	
	public String confirmTransaction(Long productTransactionId, UUID consumerId);
	
	public ProductTransaction getProductTransactionForProduct(Long productId, UUID consumerId);
}
