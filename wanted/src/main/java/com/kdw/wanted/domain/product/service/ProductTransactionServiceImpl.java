package com.kdw.wanted.domain.product.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductState;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;
import com.kdw.wanted.domain.product.dto.service.response.ProductTransactionServiceResponse;
import com.kdw.wanted.domain.product.dto.service.response.ProductTransactionServiceResponse.Transactions;
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
		if(productTransactionRepository.existsByProductIdAndConsumerId(productId, consumerId)) {
			throw new ProductTransactionException(ErrorCode.TRANSACTION_CONPLICT);
		}
		if(product.getRemaining() < 1) {
			throw new ProductTransactionException(ErrorCode.TRANSACTION_NOT_ACCEPTABLE);
		}
		product.decreaseRemaining();
		if(product.getRemaining() < 1) {
			product.stateToReserved();
		}
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
	@Transactional
	public ProductTransactionServiceResponse.Transactions getTransactions(UUID accountId) {
		List<ProductTransaction> productTransactions = productTransactionRepository.findAllByConsumerId(accountId);
		productRepository.findAllByAccountId(accountId).stream()
														.flatMap(product->product.getProductTransactions().stream())
														.forEach(transaction->productTransactions.add(transaction));
		
		Transactions transactions = new Transactions();
		productTransactions.stream().forEach(transaction->{
			if(transaction.getState() == ProductTransactionState.COMPLETE) {
				transactions.getComplete().add(transaction);
			}else {
				transactions.getInProgress().add(transaction);
			}
		});
		
		return transactions;
	}
	


	@Override
	@Transactional
	public String approveTransaction(Long productTransactionId, UUID providerId) {
		ProductTransaction productTransaction = productTransactionRepository.findById(productTransactionId).orElseThrow(()->new ProductTransactionException(ErrorCode.TRANSACTION_NOT_FOUND));
		if(!productTransaction.getProduct().getAccount().getId().equals(providerId)) {
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
	public String confirmTransaction(Long productTransactionId, UUID consumerId) {
		ProductTransaction productTransaction = productTransactionRepository.findById(productTransactionId).orElseThrow(()->new ProductTransactionException(ErrorCode.TRANSACTION_NOT_FOUND));
		if(!productTransaction.getConsumer().getId().equals(consumerId)) {
			throw new AccountException(ErrorCode.UNAUTHORIZED_ACCOUNT);
		}
		if(!productTransaction.getState().equals(ProductTransactionState.ACCEPTED)) {
			throw new ProductTransactionException(ErrorCode.TRANSACTION_NOT_ACCEPTABLE);
		}
		productTransaction.setState(ProductTransactionState.COMPLETE);
		if(productTransactionRepository.countByProductIdAndState(productTransaction.getProduct().getId(), ProductTransactionState.COMPLETE) == productTransaction.getProduct().getQuantity()) {
			productTransaction.getProduct().stateToComplete();
		}
		
		return "success";
	}

	@Override
	public ProductTransaction getProductTransactionForProduct(Long productId, UUID consumerId) {
		ProductTransaction productTransaction = productTransactionRepository.findByProductIdAndConsumerId(productId, consumerId).orElseThrow(()->new ProductTransactionException(ErrorCode.TRANSACTION_NOT_FOUND));
		return productTransaction;
	}
	
}
