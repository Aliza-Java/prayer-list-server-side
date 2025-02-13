package com.aliza.davening.rest;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.UserService;

@RestController
@RequestMapping("user")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")
public class UserWebService {

	@Autowired
	UserService userService;

	@Autowired
	EmailSender emailSender;

	// tested
	@RequestMapping(path = "getmynames/{email}")
	public List<Davenfor> getUserDavenfors(@PathVariable String email) {
		return userService.getAllUserDavenfors(email);
	}

	// tested
	@PostMapping(path = "{email}")
	public Davenfor addDavenfor(@RequestBody Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, IOException, EmailException {//TODO*: add tests for last 3 exceptions
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
	//TODONOW: need to redirect user to something after extended
	@RequestMapping("extend/{davenforId}/{email}")
	public void extendDavenfor(@PathVariable long davenforId, @PathVariable("email") String email)
			throws ObjectNotFoundException, PermissionException, EmptyInformationException {
		userService.extendDavenfor(davenforId, email);
	}

	//TODONOW: in future, make deleteName landing page like chatGpt suggested. with an 'are you sure' button 
	// tested 
	//TODONOW: link from email also gives list of all user davenfors.  not nice.  make a landing page. 
	@RequestMapping("delete/{id}/{email}") //since handled through email (for now), delete mapping is not supported
	public List<Davenfor> deleteDavenfor(@PathVariable long id, @PathVariable("email") String email)
			throws ObjectNotFoundException, PermissionException {
		return userService.deleteDavenfor(id, email);
	}

	// tested
	@RequestMapping(path = "categories")
	public List<Category> findAllCategories() throws ObjectNotFoundException {
		return userService.getAllCategories();
	}

	//TODO*: test?
	@RequestMapping(path = "category/{id}")
	public Category findCategory(@PathVariable long id) throws ObjectNotFoundException {
		return userService.getCategory(id);
	}
	
	//TODO*: test
	@PostMapping(path = "unsubscribe/{token}")
	public  boolean unsubscribe(@PathVariable String token) throws EmptyInformationException {
		return userService.unsubscribe(token);
	}
}