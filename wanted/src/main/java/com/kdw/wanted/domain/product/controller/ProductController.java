package com.kdw.wanted.domain.product.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.product.domain.Product;
import com.kdw.wanted.domain.product.domain.ProductTransaction;
import com.kdw.wanted.domain.product.dto.controller.request.ProductReqeustDto;
import com.kdw.wanted.domain.product.dto.controller.request.ProductTransactionRequestDto;
import com.kdw.wanted.domain.product.dto.controller.response.ProductResponseDto;
import com.kdw.wanted.domain.product.dto.controller.response.ProductTransactionResponseDto;
import com.kdw.wanted.domain.product.dto.controller.response.ProductTransactionResponseDto.Transactions;
import com.kdw.wanted.domain.product.service.ProductService;
import com.kdw.wanted.domain.product.service.ProductTransactionService;
import com.kdw.wanted.global.auth.service.JwtService;
import com.kdw.wanted.global.util.BaseResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/products")
@CrossOrigin(allowedHeaders = "*", originPatterns = "*")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
	
	private final ProductService productService;
	private final ProductTransactionService productTransactionService;
	private final JwtService jwtService;
	
	// 제품 등록
	@PostMapping("")
	public ResponseEntity<BaseResponseBody> createProduct(@RequestBody @Valid ProductReqeustDto.Regist productRequestDto, HttpServletRequest httpServletRequest){
		Product product = productRequestDto.toEntity();
		product.setAccount(Account.builder().id(
						jwtService.getId(httpServletRequest)
					).build());
		return new ResponseEntity<BaseResponseBody>(
					BaseResponseBody.of(productService.createProduct(product))
					,HttpStatus.CREATED
				);
	}
	
	// 제품 리스트 
	@GetMapping("")
	public ResponseEntity<BaseResponseBody> getProductList(){
		return new ResponseEntity<BaseResponseBody>(
					BaseResponseBody.of(
							productService.getProductList().stream().map(ProductResponseDto.Element::fromEntity).toList(),
							"success")
					,HttpStatus.OK
				);
	}
	
	// 제품 상세와 해당 제품과의 거래 내역(소비자)
	@GetMapping("/{productId}")
	public ResponseEntity<BaseResponseBody> getProduct(@PathVariable("productId") Long productId, HttpServletRequest httpRequest){
		Product product = productService.getProduct(productId);
		UUID consumerId = null; 
		ProductTransaction productTransaction = null;
		if(httpRequest.getHeader("Authorization") != null) {
			consumerId = jwtService.getId(httpRequest); 
			productTransaction = productTransactionService.getProductTransactionForProduct(productId, consumerId);
		}
		
		
		return new ResponseEntity<BaseResponseBody>(
					BaseResponseBody.of(
							ProductResponseDto.Detail.fromEntity(product, productTransaction)
							,"success")
						,HttpStatus.OK
				);
	}
	
	// 거래 요청
	@PostMapping("/transactions")
	public ResponseEntity<BaseResponseBody> createTransaction(@RequestBody @Valid ProductTransactionRequestDto.Make productTransactionRequestDto, HttpServletRequest httpRequest){
		return new ResponseEntity<BaseResponseBody>(
				BaseResponseBody.of(productTransactionService.makeTransaction(productTransactionRequestDto.getProductId(), jwtService.getId(httpRequest)),
				"success"),
				HttpStatus.CREATED
				);
	}
	
	// 거래 리스트
	@GetMapping("/transactions")
	public ResponseEntity<BaseResponseBody> getTransactions(HttpServletRequest httpRequest){
		return new ResponseEntity<BaseResponseBody>(
				BaseResponseBody.of(ProductTransactionResponseDto.Transactions
											.fromServiceDto(
													productTransactionService.getTransactions(jwtService.getId(httpRequest))
											),															
				"success"),
				HttpStatus.OK
				);
	}
	
	
	// 거래 승인(판매자)
	@PutMapping("/transactions/approve")
	public ResponseEntity<BaseResponseBody> approveTransaction(@RequestBody @Valid ProductTransactionRequestDto.Approve productTransactionRequestDto, HttpServletRequest httpRequest){
		return new ResponseEntity<BaseResponseBody>(
					BaseResponseBody.of(productTransactionService.approveTransaction(productTransactionRequestDto.getProductTransactionId(), jwtService.getId(httpRequest)),
					"success"),
					HttpStatus.ACCEPTED
				);
	}
	
	// 거래 확정(구매자)
	@PutMapping("/transactions/confirm")
	public ResponseEntity<BaseResponseBody> confirmTransaction(@RequestBody @Valid ProductTransactionRequestDto.Confirm productTransactionRequestDto, HttpServletRequest httpRequest){
		return new ResponseEntity<BaseResponseBody>(
				BaseResponseBody.of(productTransactionService.confirmTransaction(productTransactionRequestDto.getProductTransactionId(), jwtService.getId(httpRequest)),
				"success"),
				HttpStatus.ACCEPTED
			);
	}
}
