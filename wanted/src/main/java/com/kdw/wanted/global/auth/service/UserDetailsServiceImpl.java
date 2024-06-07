package com.kdw.wanted.global.auth.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.domain.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final AccountRepository accountRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Account account = accountRepository.findByUsername(username).orElseThrow(()->new RuntimeException("loaduserbyusername err"));
		
		return createUserDetail(account);
	}
	
	private UserDetails createUserDetail(Account account) {
		return Account.builder()
				.username(account.getUsername())
				.password(bCryptPasswordEncoder.encode(account.getPassword()))
				.roles(account.getRoles())
				.build();
	}

}
