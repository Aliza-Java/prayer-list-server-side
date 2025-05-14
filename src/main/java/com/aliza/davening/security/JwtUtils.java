package com.aliza.davening.security;

import java.text.SimpleDateFormat;
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

	@Value("${access.token.expiration.ms}")
	private long accessTokenExpiration;

	public SecretKey getSigningKey() {
		byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateJwtToken(Authentication authentication, boolean shortLived) {
		long oneWeek = 1000 * 60 * 60 * 24 * 7;
		System.out.println("accessToken is " + accessTokenExpiration + " ms");
		System.out.println("refreshToken is " + oneWeek + " ms");
		long expiration = shortLived ? accessTokenExpiration : oneWeek;																				// refreshToken

		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

		return Jwts.builder().setSubject(userPrincipal.getUsername()).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + expiration)).signWith(getSigningKey()).compact();
	}

	public String generateJwtToken(String email, boolean shortLived) {// todo - unify both generateJwtTokens
		
		long oneWeek = 1000 * 60 * 60 * 24 * 7;
		long expiration = shortLived ? accessTokenExpiration : oneWeek;
		return Jwts.builder().setSubject(email).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + expiration)).signWith(getSigningKey()).compact();
	}

	public String extractEmailFromToken(String authToken) {
		try {
			return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken).getBody()
					.getSubject();

		} catch (ExpiredJwtException e) {
			throw e;
		} catch (UnsupportedJwtException e) {
			System.err.println("JWT token is unsupported: " + e.getMessage());
		} catch (MalformedJwtException e) {
			System.err.println("JWT token is invalid: " + e.getMessage());
		} catch (SignatureException e) {
			System.err.println("JWT signature is invalid: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.err.println("JWT claims string is empty: " + e.getMessage());
		}
		return null; // Return null if the token is invalid
	}

	public String generateEmailToken(String email, Date expiration) {
		return Jwts.builder().setSubject(email).setExpiration(expiration).signWith(getSigningKey()).compact();
	}

	public boolean validateJwtToken(String authToken, String name) {
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error(name + " token is expired: {}", e.getMessage());
			throw e;
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}

	public String generateDirectAdminToken(String email, Date expiry) {

		System.out.println(getExpiryNotice(expiry));

		return Jwts.builder().setSubject(email).setIssuedAt(new Date()).setExpiration(expiry) // 24 hours ahead
				.signWith(getSigningKey()).compact();
	}

	public String getExpiryNotice(Date expiry) {
		SimpleDateFormat formatter = new SimpleDateFormat("(EEEE) HH:mm");
		return "Your token will expire tomorrow " + formatter.format(expiry);
	}
}