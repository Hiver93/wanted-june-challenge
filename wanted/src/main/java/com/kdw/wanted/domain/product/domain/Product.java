package com.kdw.wanted.domain.product.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.enums.ProductState;
import com.kdw.wanted.domain.product.domain.util.ProductStateConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity(name="product")
@EntityListeners(AuditingEntityListener.class)
public class Product {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	Long id;
	
	@Column
	String name;
	
	@Column
	Long price;
	
	@Column
	Long quantity;
	
	@Column
	Long remaining;
	
	@Convert(converter = ProductStateConverter.class)
	@Builder.Default
	ProductState state = ProductState.SALE;
	
	@CreatedDate
	LocalDateTime createdAt;
	
	@LastModifiedDate
	LocalDateTime updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
	Account account;
	
	@Version
	Integer version;
	
	
	public void decreaseRemaining() {
		this.remaining -= 1;
	}
	
	
	@OneToMany(mappedBy = "product", targetEntity = ProductTransaction.class)
    private List<ProductTransaction> productTransactions = new ArrayList<>();
}
