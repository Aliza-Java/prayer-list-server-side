package com.aliza.davening.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.security.LoginRequest;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.UserService;

@RestController
@RequestMapping("user")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")

public class UserWebService {

	public final String client = SchemeValues.client;

	@Autowired
	UserService userService;

	@Autowired
	EmailSender emailSender;

	@Autowired
	JwtUtils jwtUtils;

	// tested
	@RequestMapping(path = "getmynames/{email}")
	public List<Davenfor> getUserDavenfors(@PathVariable String email) throws ObjectNotFoundException {
		return userService.getAllUserDavenfors(email);
	}

	// tested
	@PostMapping(path = "{email}")
	public boolean addDavenfor(@RequestBody Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, EmailException {// TODO*: add tests
																						// for last 3
																						// exceptions
		return userService.addDavenfor(davenfor, email);
	}

	// tested
	@PutMapping(path = "updatename/{email}")
	// TODO*: add test for 'validity' of Davenfor being passed in
	public Davenfor updateDavenfor(@RequestBody @Valid Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, PermissionException {
		return userService.updateDavenfor(davenfor, email, false);
	}

	// tested
	@RequestMapping("extend/{davenforId}/{email}")
	public void extendDavenfor(@PathVariable long davenforId, @PathVariable("email") String email)
			throws ObjectNotFoundException, PermissionException, EmptyInformationException {
		userService.extendDavenfor(davenforId, email);
	}

	@DeleteMapping("delete/{id}/{email}") // this method is called from the website
	public List<Davenfor> deleteDavenfor(@PathVariable long id, @PathVariable("email") String email)
			throws ObjectNotFoundException, PermissionException {
		return userService.deleteDavenfor(id, email, false);
	}

	// tested
	@RequestMapping(path = "categories")
	public List<Category> findAllCategories() throws ObjectNotFoundException {
		return userService.getAllCategories();
	}

	// TODO*: test?
	@RequestMapping(path = "category/{id}")
	public Category findCategory(@PathVariable long id) throws ObjectNotFoundException {
		return userService.getCategory(id);
	}

	// TODO*: test
	@GetMapping(path = "unsubscribe/request", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> requestToUnsubscribe(@RequestParam String email) {
		String message = emailSender.requestToUnsubscribe(email);
		Map<String, String> response = new HashMap<>();
		response.put("message", message);
		return ResponseEntity.ok(response);
	}

	@PostMapping("request-otp")
	public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		try {
			userService.setNewOtp(email);
			return ResponseEntity.ok().build();
		} catch (EmailException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (ObjectNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
		}
	}

	@PostMapping("verify-otp")
	public ResponseEntity<List<Davenfor>> verifyOtp(@RequestBody LoginRequest request) {
		try {
			userService.verifyOtp(request.getUsername(), request.getPassword());
			return ResponseEntity.ok(userService.getAllUserDavenfors(request.getUsername()));
		} catch (ObjectNotFoundException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		} catch (PermissionException ex) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}
}