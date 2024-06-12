package com.kdw.wanted.global.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	
	
	// Global 에러 코드
	// 400
	INVALID_PARAMETER(BAD_REQUEST,"유효하지 않은 파라미터 값입니다."),
	
	// 500
	UNKNOWN_EXCEPTION(INTERNAL_SERVER_ERROR,"알 수 없는 예외가 발생하였습니다. 관리자에게 문의하세요.");
	
	private final HttpStatus HttpStatus;
	private final String message;
}
