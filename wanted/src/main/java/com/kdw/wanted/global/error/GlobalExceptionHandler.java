package com.kdw.wanted.global.error;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kdw.wanted.global.error.exception.AccountException;
import com.kdw.wanted.global.error.exception.AuthException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 원하는 예외가 발생하지 않음
	@ExceptionHandler(value= {AccountException.class})
	public ResponseEntity<ErrorResponseBody> accountException(AccountException e){
		log.info("occured location : {}, message : {}, cause : {}", e.getStackTrace()[0], e.getMessage(), e.getCause());
		return ErrorResponseBody.toResponseEntity(e.getErrorCode());
	}
	
	@ExceptionHandler(value = {AuthException.class, AuthenticationException.class})
	public ResponseEntity<ErrorResponseBody> authException(Exception e){
		log.info("occured location : {}, message : {}, cause : {}", e.getStackTrace()[0], e.getMessage(), e.getCause());
		if(e instanceof AuthException) {
			return ErrorResponseBody.toResponseEntity(((AuthException)e).getErrorCode());
		}else if(e instanceof BadCredentialsException) {
			return ErrorResponseBody.toResponseEntity(ErrorCode.INVALID_CREDENTIALS);
		}else {
			return unknownException(e);
		}
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseBody> invalidPrameter(MethodArgumentNotValidException e){
		log.info("occured location : {}, message : {}, cause : {}", e.getStackTrace()[0], e.getMessage(), e.getCause());
		return ErrorResponseBody.toResponseEntity(ErrorCode.INVALID_PARAMETER);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseBody> unknownException(Exception e) {
		log.error("occured location : {}, message : {}, cause : {}", e.getStackTrace()[0], e.getMessage(), e.getCause());
		return ErrorResponseBody.toResponseEntity(ErrorCode.UNKNOWN_EXCEPTION);
	}
}
