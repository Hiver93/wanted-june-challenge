package com.kdw.wanted.domain.account.service;

import com.kdw.wanted.domain.account.domain.Account;
import com.kdw.wanted.global.auth.dto.JwtToken;


public interface AccountService {
	public String signup(Account account);
	public JwtToken signin(Account account);
}
