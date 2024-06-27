package com.kdw.wanted.domain.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;

public interface ProductTransactionRepository extends JpaRepository<ProductTransaction, Long> {
	public Optional<ProductTransaction> findByConsumerId(UUID consumerId);
	public List<ProductTransaction> findAllByConsumerId(UUID consumerId);
	public Optional<ProductTransaction> findByProductIdAndConsumerId(Long productId, UUID consumerId);
	public Boolean existsByProductIdAndConsumerId(Long productId, UUID consumerId);
	public Long countByProductIdAndState(Long productId, ProductTransactionState state);
}
