package com.kdw.wanted.domain.account.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kdw.wanted.domain.product.domain.ProductTransaction;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements UserDetails {
	
	@Id @Column(unique=true)
	private UUID id;
	
	@Column
	private String username;
	
	@Column
	private String password;

	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private List<String> roles = new ArrayList<>();
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream()
				.map(SimpleGrantedAuthority::new)
				.toList();
	}
	
	@OneToMany(mappedBy = "provider")
    private List<ProductTransaction> providerTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "consumer")
    private List<ProductTransaction> consumerTransactions = new ArrayList<>();
	
}
