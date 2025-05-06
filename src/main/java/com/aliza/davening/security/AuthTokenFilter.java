package com.aliza.davening.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

public class AuthTokenFilter extends OncePerRequestFilter {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		// login and new signup don't need a token check - they create it.
		// direct preview and direct send handle their own token check in their
		// controller.
		List<String> noCheckPaths = List.of("/dlist/auth/signin", "/dlist/auth/signup", "/dlist/direct/preview",
				"/dlist/direct/send");
		if (!noCheckPaths.contains(path)) {
			try {
				String jwt = parseJwt(request);
				if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
					String username = jwtUtils.extractEmailFromToken(jwt);

					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (ExpiredJwtException e) {
				logger.warn("JWT token is expired: {}", e.getMessage());
				// avoiding using UNAUTHORIZED because it redirects in the frontend
				response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "Token expired");
				return;
			} catch (Exception e) {
				logger.error("Cannot set user authentication: {}", e.getMessage());
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}
}