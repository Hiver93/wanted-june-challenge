package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;
import com.kdw.wanted.domain.product.dto.request.ProductTransactionRequestDto;
import com.kdw.wanted.domain.product.dto.response.ProductTransactionResponseDto;
import com.kdw.wanted.domain.product.repository.ProductRepository;
import com.kdw.wanted.domain.product.repository.ProductTransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductTransactionServiceImpl implements ProductTransactionService{
	
	private final ProductTransactionRepository productTransactionRepository;
	private final ProductRepository productRepository;
	
	@Override
	@Transactional
	public String makeTransaction(ProductTransactionRequestDto.Make productTransactionRequestDto, UUID consumerId) {
		Product product = productRepository.findById(productTransactionRequestDto.getProductId()).orElseThrow();
		if(product.getAccount().getId().equals(consumerId)){
			throw new RuntimeException();
		}
		if(product.getRemaning() < 1) {
			throw new RuntimeException();
		}
		product.setRemaning(product.getRemaning()-1);
		ProductTransaction productTransaction = ProductTransaction.builder()
													.price(product.getPrice())
													.product(product)
													.consumer(Account.builder()
																.id(consumerId)
																.build())
													.build();
		productTransactionRepository.save(productTransaction);
		return "success";
	}

	@Override
	public List<ProductTransactionResponseDto.Element> getTransactins(UUID accountId) {
		List<ProductTransaction> productTransactions = productTransactionRepository.findAllByConsumerId(accountId);
		productRepository.findAllByAccountId(accountId).stream().forEach(
					p-> p.getProductTransactions().stream().forEach(
								t -> {productTransactions.add(t);}
							)
				);
		return productTransactions.stream().map(ProductTransactionResponseDto.Element::fromEntity).toList();
	}
	


	@Override
	@Transactional
	public String approveTransaction(ProductTransactionRequestDto.Approve productTransactionRequestDto, UUID producerId) {
		ProductTransaction productTransaction = productTransactionRepository.findById(productTransactionRequestDto.getProductTransactionId()).orElseThrow(()->new RuntimeException());
		if(!productTransaction.getProduct().getAccount().getId().equals(producerId)) {
			throw new RuntimeException();
		}
		productTransaction.setState(ProductTransactionState.ACCEPTED);
		
		return "success";
	}
	
}
