package com.kdw.wanted.global.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BaseResponseBody {
	private Object data;
	private String message;
	
	public static BaseResponseBody of(String message) {
		return BaseResponseBody.builder()
				.message(message)
				.build();
	}
	
	public static BaseResponseBody of(Object data, String message) {
		return BaseResponseBody.builder()
				.data(data)
				.message(message)
				.build();
	}
}
