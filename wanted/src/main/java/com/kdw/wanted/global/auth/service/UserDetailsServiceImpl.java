package com.kdw.wanted.global.auth.service;

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
		Account account = accountRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
		
		return createUserDetail(account);
	}
	
	private UserDetails createUserDetail(Account account) {
		return Account.builder()
				.id(account.getId())
				.username(account.getUsername())
				.password(bCryptPasswordEncoder.encode(account.getPassword()))
				.roles(account.getRoles())
				.build();
	}

}
