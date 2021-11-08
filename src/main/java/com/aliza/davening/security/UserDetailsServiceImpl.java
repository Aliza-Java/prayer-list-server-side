package com.aliza.davening.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.repositories.AdminRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	AdminRepository adminRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Admin admin = adminRepository.getAdminByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Admin not Found with email: " + email));

		return UserDetailsImpl.build(admin);
	}

}