package com.kdw.wanted.domain.product.domain.enums;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ProductTransactionState {
	RESERVED("예약중",1), ACCEPTED("판매승인",2), COMPLETE("판매완료",3), CANCELED("취소", 4);
	
	private String desc;
	private int code;
	
	private ProductTransactionState(String desc, int code) {
		this.desc = desc;
		this.code = code;
	}
	
	public static ProductTransactionState ofCode(int code) {
		return Arrays.stream(ProductTransactionState.values())
				.filter(v -> v.getCode() == code)
				.findAny()
				.orElseThrow(()->new RuntimeException("잘못된 코드"));
	}
}
