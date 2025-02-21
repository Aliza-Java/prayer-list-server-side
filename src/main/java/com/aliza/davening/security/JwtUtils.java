package com.aliza.davening.security;

import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration.ms}")
	private int jwtExpirationMs;

	public SecretKey getSigningKey() {
		byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateJwtToken(Authentication authentication) {

		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

		return Jwts.builder().setSubject(userPrincipal.getUsername()).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(getSigningKey()).compact();
	}

	public String getUserNameFromJwtToken(String token) {
		try {
			return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody()
					.getSubject();
			
		} catch (ExpiredJwtException e) {
			System.err.println("JWT token is expired: " + e.getMessage());
		} catch (UnsupportedJwtException e) {
			System.err.println("JWT token is unsupported: " + e.getMessage());
		} catch (MalformedJwtException e) {
			System.err.println("JWT token is invalid: " + e.getMessage());
		} catch (SignatureException e) {
			System.err.println("JWT signature is invalid: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.err.println("JWT claims string is empty: " + e.getMessage());
		}
		return null; // Return null if the token is invalid or expired
	}

	public String generateUnsubscribeToken(String email) {
		return Jwts.builder().setSubject(email).signWith(getSigningKey()).compact();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}