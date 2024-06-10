package com.kdw.wanted.domain.product.domain.enums;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ProductState {
	SALE("판매중",1), RESERVED("예약중",2), COMPLETE("판매완료",3), CANCELED("취소", 4);
	
	private String desc;
	private int code;
	
	private ProductState(String desc, int code) {
		this.desc = desc;
		this.code = code;
	}
	
	public static ProductState ofCode(int code) {
		return Arrays.stream(ProductState.values())
				.filter(v -> v.getCode() == code)
				.findAny()
				.orElseThrow(()->new RuntimeException("잘못된 코드"));
	}
}
