package com.kdw.wanted.domain.product.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;
import com.kdw.wanted.domain.product.domain.util.ProductTransactionStateConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity(name = "product_transaction")
@EntityListeners(AuditingEntityListener.class)
public class ProductTransaction {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@Column
	Long price;
	
	@Convert(converter = ProductTransactionStateConverter.class)
	@Builder.Default
	ProductTransactionState state = ProductTransactionState.SALE;

	@CreatedDate
	LocalDateTime createdAt;
	
	@LastModifiedDate
	LocalDateTime updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "consumer_id")
	Account consumer;
	
	@ManyToOne
	@JoinColumn(name = "product_id")
	Product product;
}
