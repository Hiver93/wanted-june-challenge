package com.kdw.wanted.global.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	
	
	// Global
	// 400
	INVALID_PARAMETER(BAD_REQUEST,"유효하지 않은 파라미터 값입니다."),
	// 500
	UNKNOWN_EXCEPTION(INTERNAL_SERVER_ERROR,"알 수 없는 예외가 발생하였습니다. 관리자에게 문의하세요."),
	
	
	// Auth
	// 401 JWT 관련
	INVALID_JWT_TOKEN(UNAUTHORIZED,"유효하지 않은 JWT토큰입니다."),
	EXPIRED_JWT_TOKEN(UNAUTHORIZED,"기한이 지난 JWT토큰입니다."),
	UNSUPPORTED_JWT_TOKEN(UNAUTHORIZED,"지원하지 않는 JWT토큰입니다."),
	JWT_CLAIMS_EMPTY(UNAUTHORIZED,"JWT claims가 비었습니다."),
	// 401 유저 정보 관련
	INVALID_CREDENTIALS(UNAUTHORIZED, "유저네임 또는 비밀번호가 잘못되었습니다."),
	
	// 409 
	USER_NAME_CONPLICT(CONFLICT, "이미 존재하는 유저 아이디입니다.");
	
	
	private final HttpStatus HttpStatus;
	private final String message;
}
