package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.dto.request.ProductTransactionRequestDto;
import com.kdw.wanted.domain.product.dto.response.ProductTransactionResponseDto;

public interface ProductTransactionService {
	public String makeTransaction(ProductTransactionRequestDto.Make productTransactionRequestDto, UUID consumerID);
	
	public List<ProductTransactionResponseDto.Element> getTransactins(UUID accountId);
	
	public String approveTransaction(ProductTransactionRequestDto.Approve productTransactionRequestDto, UUID producerId);
	
	public String confirmTransaction(ProductTransactionRequestDto.Confirm productTransactionRequestDto, UUID consumerId);
}
