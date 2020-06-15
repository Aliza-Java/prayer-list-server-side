package com.aliza.davening;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.aliza.davening.services.AdminService;

@EnableWebSecurity
public class ApiSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	AdminService adminService;

	// TODO: change and put in application.properties whatever doesn't need auth
	// (like login).  For now inserting but allowing all, since Spring Security requires auth and I need to bypass it.
	//TODO: put this in parameters of antMatchers: HttpMethod.GET, SIGN_UP_URL
	public static final String SIGN_UP_URL = "/**";

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/**", SIGN_UP_URL).permitAll().anyRequest().authenticated();
	}
	
	//

	// TODO: Not sure what this code is for. Copied from
	// https://www.appsdeveloperblog.com/encrypt-user-password-with-spring-security/
	// TODO: Delete if works without
	/*
	 * @Override public void configure(AuthenticationManagerBuilder auth) throws
	 * Exception {
	 * auth.adminService(adminService).passwordEncoder(bCryptPasswordEncoder); }
	 */
}