package com.kdw.wanted.domain.product.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kdw.wanted.domain.product.domain.Product;

import jakarta.persistence.LockModeType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	public List<Product> findAllByAccountId(UUID accountId);
	
	 @Lock(LockModeType.PESSIMISTIC_WRITE)    
    Optional<Product> findWithPessimisticLockById(Long id);
}
