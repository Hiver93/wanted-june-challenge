package com.kdw.wanted.domain.product.domain.util;

import org.hibernate.type.descriptor.converter.internal.JpaAttributeConverterImpl;

import com.kdw.wanted.domain.product.domain.enums.ProductState;

import jakarta.persistence.AttributeConverter;

public class ProductStateConverter implements AttributeConverter<ProductState, Integer> {

	@Override
	public Integer convertToDatabaseColumn(ProductState attribute) {
		return attribute.getCode();
	}

	@Override
	public ProductState convertToEntityAttribute(Integer dbData) {
		return ProductState.ofCode(dbData);
	}

}
