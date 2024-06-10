package com.kdw.wanted.domain.product.domain.util;

import com.kdw.wanted.domain.product.domain.enums.ProductState;
import com.kdw.wanted.domain.product.domain.enums.ProductTransactionState;

import jakarta.persistence.AttributeConverter;

public class ProductTransactionStateConverter implements AttributeConverter<ProductTransactionState, Integer> {

	@Override
	public Integer convertToDatabaseColumn(ProductTransactionState attribute) {
		return attribute.getCode();
	}

	@Override
	public ProductTransactionState convertToEntityAttribute(Integer dbData) {
		return ProductTransactionState.ofCode(dbData);
	}

}
