package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;
import com.kdw.wanted.domain.product.dto.request.ProductTransactionRequestDto;
import com.kdw.wanted.domain.product.dto.request.ProductTransactionRequestDto.Confirm;
import com.kdw.wanted.domain.product.dto.response.ProductTransactionResponseDto;
import com.kdw.wanted.domain.product.repository.ProductRepository;
import com.kdw.wanted.domain.product.repository.ProductTransactionRepository;
import com.kdw.wanted.global.auth.service.JwtService;
import com.kdw.wanted.global.error.ErrorCode;
import com.kdw.wanted.global.error.exception.AccountException;
import com.kdw.wanted.global.error.exception.ProductException;
import com.kdw.wanted.global.error.exception.ProductTransactionException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductTransactionServiceImpl implements ProductTransactionService{
	
	private final ProductTransactionRepository productTransactionRepository;
	private final ProductRepository productRepository;
	private final JwtService jwtService;
	
	@Override
	@Transactional
	public String makeTransaction(Long productId, UUID consumerId) {
		Product product = productRepository.findWithPessimisticLockById(productId).orElseThrow(()->new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
		if(product.getAccount().getId().equals(consumerId)){
			throw new AccountException(ErrorCode.UNAUTHORIZED_ACCOUNT);
		}
		if(product.getRemaining() < 1) {
			throw new ProductTransactionException(ErrorCode.TRANSACTION_NOT_ACCEPTABLE);
		}
		product.decreaseRemaining();
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
	public List<ProductTransaction> getTransactins(UUID accountId) {
		List<ProductTransaction> productTransactions = productTransactionRepository.findAllByConsumerId(accountId);
		productRepository.findAllByAccountId(accountId).stream().forEach(
					p-> p.getProductTransactions().stream().forEach(
								t -> {productTransactions.add(t);}
							)
				);
		return productTransactions;
	}
	


	@Override
	@Transactional
	public String approveTransaction(ProductTransactionRequestDto.Approve productTransactionRequestDto, UUID producerId) {
		ProductTransaction productTransaction = productTransactionRepository.findById(productTransactionRequestDto.getProductTransactionId()).orElseThrow(()->new ProductTransactionException(ErrorCode.TRANSACTION_NOT_FOUND));
		if(!productTransaction.getProduct().getAccount().getId().equals(producerId)) {
			throw new AccountException(ErrorCode.UNAUTHORIZED_ACCOUNT);
		}
		if(!productTransaction.getState().equals(ProductTransactionState.RESERVED)) {
			throw new ProductTransactionException(ErrorCode.TRANSACTION_NOT_ACCEPTABLE);
		}
		productTransaction.setState(ProductTransactionState.ACCEPTED);
		
		return "success";
	}

	@Override
	@Transactional
	public String confirmTransaction(Confirm productTransactionRequestDto, UUID consumerId) {
		ProductTransaction productTransaction = productTransactionRepository.findById(productTransactionRequestDto.getProductTransactionId()).orElseThrow(()->new ProductTransactionException(ErrorCode.TRANSACTION_NOT_FOUND));
		if(!productTransaction.getConsumer().getId().equals(consumerId)) {
			throw new AccountException(ErrorCode.UNAUTHORIZED_ACCOUNT);
		}
		if(!productTransaction.getState().equals(ProductTransactionState.ACCEPTED)) {
			throw new ProductTransactionException(ErrorCode.TRANSACTION_NOT_ACCEPTABLE);
		}
		productTransaction.setState(ProductTransactionState.COMPLETE);
		
		return "success";
	}

	@Override
	public ProductTransaction getProductTransactionForProduct(Long productId, UUID consumerId) {
		ProductTransaction productTransaction = productTransactionRepository.findByProductIdAndConsumerId(productId, consumerId).orElseThrow(()->new ProductTransactionException(ErrorCode.TRANSACTION_NOT_FOUND));
		return productTransaction;
	}
	
}
