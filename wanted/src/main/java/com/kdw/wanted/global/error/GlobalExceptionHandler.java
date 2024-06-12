package com.kdw.wanted.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.kdw.wanted.global.error.exception.AccountException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseBody> unknownException(Exception e, WebRequest r) {
		log.error("occured location : {}, message : {}", e.getStackTrace()[0], e.getMessage());
		return ErrorResponseBody.toResponseEntity(ErrorCode.UNKNOWN_EXCEPTION);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseBody> invalidPrameter(MethodArgumentNotValidException e){
		log.info(e.getMessage());
		return ErrorResponseBody.toResponseEntity(ErrorCode.INVALID_PARAMETER);
	}
	
	@ExceptionHandler(AccountException.class)
	public ResponseEntity<ErrorResponseBody> accountException(AccountException e){
		log.info(e.getMessage());
		return ErrorResponseBody.toResponseEntity(e.getErrorCode());
	}
}
