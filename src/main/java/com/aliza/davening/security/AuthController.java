package com.aliza.davening.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.services.AdminService;

import io.jsonwebtoken.ExpiredJwtException;

@CrossOrigin(origins = ("${client.origin}"), maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	AdminService adminService;

	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/signin")
	// TODO*: test 'validity' of LoginRequest being passed in
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		Authentication authentication = authenticationManager.authenticate( //todo*: catch BadCredentialsException nicely, maybe in handler
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String accessToken = jwtUtils.generateJwtToken(authentication, true);
		String refreshToken = jwtUtils.generateJwtToken(authentication, false);
		//refresh token replaces old on by user if same path and domain

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		// Send refresh token as secure cookie
		attachRefreshToCookie(response, refreshToken);

		return ResponseEntity.ok(new JwtResponse(accessToken, userDetails.getId(), userDetails.getUsername()));
	}

	public void attachRefreshToCookie(HttpServletResponse response, String refreshToken) {
		// Works with newer version of Servlet, or Spring boot 3
		//Cookie cookie = new Cookie("refreshToken", refreshToken);
		//cookie.setHttpOnly(true); // Not accessible via JS
		//cookie.setSecure(false); // Only sent over HTTPS
		//cookie.setPath("/"); // Sent only to this endpoint
		//cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days in seconds
		//response.addCookie(cookie);
		
		String cookieValue = "refreshToken=" + refreshToken
				+ "; HttpOnly; "
				//+ "Secure; "
				//+ "SameSite=None; "
				+ "Path=/; "
				//+ "domain=myapp.com; " +   //uncomment this line for production
				+ "Max-Age=" + (7 * 24 * 60 * 60);

		response.setHeader("Set-Cookie", cookieValue);

	}

	@PostMapping("/refresh")
	public ResponseEntity<JwtResponse> refresh(HttpServletRequest request) {
		// Get the cookies from the request
		Cookie[] cookies = request.getCookies();

		// Check if the cookies are present
		if (cookies != null) {
			// Loop through cookies to find the refresh token
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					String refreshToken = cookie.getValue();
					// Now you can use the refresh token for validation

					return validateRefreshToken(refreshToken);
				}
			}
		}

		// If no refresh token was found, return an error
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	// Method to validate and issue new access token using the refresh token
	private ResponseEntity<JwtResponse> validateRefreshToken(String refreshToken) { 
		//TODO* - test: after login, send request:
			//1. when both access and refresh are valid
			//2. when access is invalid and refresh is still valid (re-issues a new access token)
			//3. when both are invalid - logs out
		String email;
		try {
			email = jwtUtils.extractEmailFromToken(refreshToken);
			if (email == null)
			{
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			} else {
				String newAccessToken = jwtUtils.generateJwtToken(email, true);
				return ResponseEntity.ok(new JwtResponse(newAccessToken, 0, email)); // id doesn't really matter here
			}
		} catch (ExpiredJwtException e) {
			System.err.println("Refresh token is expired");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping(path = "/signup")

	public ResponseEntity<?> setAdmin(@RequestBody LoginRequest credentials) throws DatabaseException {
		if (adminService.setAdmin(credentials)) {
			return ResponseEntity.ok("New admin registered successfully!");
		}
		return ResponseEntity.badRequest().body("Could not create new admin.");
	}

//	public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
//		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//			return ResponseEntity
//					.badRequest()
//					.body(new MessageResponse("Error: Username is already taken!"));
//		}
//
//		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//			return ResponseEntity
//					.badRequest()
//					.body(new MessageResponse("Error: Email is already in use!"));
//		}
//
//		// Create new user's account
//		User user = new User(signUpRequest.getUsername(), 
//							 signUpRequest.getEmail(),
//							 encoder.encode(signUpRequest.getPassword()));
//
//		Set<String> strRoles = signUpRequest.getRole();
//		Set<Role> roles = new HashSet<>();
//
//		if (strRoles == null) {
//			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//			roles.add(userRole);
//		} else {
//			strRoles.forEach(role -> {
//				switch (role) {
//				case "admin":
//					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
//							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//					roles.add(adminRole);
//
//					break;
//				case "mod":
//					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
//							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//					roles.add(modRole);
//
//					break;
//				default:
//					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//					roles.add(userRole);
//				}
//			});
//		}
//
//		user.setRoles(roles);
//		userRepository.save(user);
//
//		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
//	}
}