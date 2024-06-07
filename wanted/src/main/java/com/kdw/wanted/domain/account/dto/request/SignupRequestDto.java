package com.kdw.wanted.domain.account.dto.request;

import com.kdw.wanted.domain.account.domain.Account;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequestDto {
	
	@NotBlank
	String username;
	
	@NotBlank
	String password;
	
	public Account toEntity() {
		return Account.builder()
				.username(username)
				.password(password)
				.build();
	}
}
