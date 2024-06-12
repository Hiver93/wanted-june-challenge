package com.kdw.wanted.global.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.kdw.wanted.global.auth.filter.JwtAuthenticationFilter;
import com.kdw.wanted.global.auth.service.JwtTokenProvider;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(
				httpBasic->
					httpBasic.disable())
			.csrf(
				csrfConfig->
					csrfConfig.disable()
					)
			.sessionManagement(
				sessionManage->
					sessionManage.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.headers(
					headerConifg->
						headerConifg
							.frameOptions(frameOptionsConfig->frameOptionsConfig.disable())
					)
			.authorizeHttpRequests(
					authorizeRequests -> 
						authorizeRequests
							.requestMatchers("/","/accounts/signin", "/accounts/signup").permitAll()
							.requestMatchers("/accounts/signin").permitAll()
							.requestMatchers(HttpMethod.GET, "/products").permitAll()
							.requestMatchers("/error").permitAll()
							.anyRequest().authenticated()
					)
			.addFilterBefore(
					new JwtAuthenticationFilter(jwtTokenProvider), 
					UsernamePasswordAuthenticationFilter.class);
			
		
		
		return http.build();
	}
}
