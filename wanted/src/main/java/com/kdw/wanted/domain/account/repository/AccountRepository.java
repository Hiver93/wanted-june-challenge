package com.kdw.wanted.domain.account.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kdw.wanted.domain.account.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>{

	boolean existsByUsername(String username);
	Optional<Account> findByUsername(String username);
}
