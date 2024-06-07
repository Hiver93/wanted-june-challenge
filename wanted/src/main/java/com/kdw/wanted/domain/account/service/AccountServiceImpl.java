package com.kdw.wanted.domain.account.service;

import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.account.repository.AccountRepository;
import com.kdw.wanted.global.auth.dto.JwtToken;
import com.kdw.wanted.global.auth.service.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
	private final AccountRepository accountRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public String signup(Account account) {
		if(accountRepository.existsByUsername(account.getUsername())) {
			throw new RuntimeException();
		}
		account.setId(UUID.randomUUID());
		account.getRoles().add("USER");
		accountRepository.save(account);
		return "success";
	}

	@Override
	public JwtToken signin(Account account) {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(account.getUsername(), account.getPassword());
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		JwtToken jwtToken = jwtTokenProvider.genrateToken(authentication);
	
		return jwtToken;
	}
}
