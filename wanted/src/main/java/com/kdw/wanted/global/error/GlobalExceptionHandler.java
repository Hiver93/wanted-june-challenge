package com.kdw.wanted.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.kdw.wanted.global.error.exception.AccountException;
import com.kdw.wanted.global.error.exception.AuthException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(AuthException.class)
	public ResponseEntity<ErrorResponseBody> authException(AuthException e){
		return ErrorResponseBody.toResponseEntity(e.getErrorCode());
	}
	
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseBody> invalidPrameter(MethodArgumentNotValidException e){
		log.info(e.getMessage());
		return ErrorResponseBody.toResponseEntity(ErrorCode.INVALID_PARAMETER);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseBody> unknownException(Exception e) {
		log.error("occured location : {}, message : {}, cause : {}", e.getStackTrace()[0], e.getMessage(), e.getCause());
		return ErrorResponseBody.toResponseEntity(ErrorCode.UNKNOWN_EXCEPTION);
	}
}
