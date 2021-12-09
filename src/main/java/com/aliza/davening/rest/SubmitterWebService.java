package com.aliza.davening.rest;

import java.util.List;

import javax.mail.MessagingException;
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
import com.aliza.davening.services.SubmitterService;

@RestController
@RequestMapping("sub")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials="true")
public class SubmitterWebService {

	@Autowired
	SubmitterService submitterService;
	
	@Autowired 
	EmailSender emailSender;

	@RequestMapping(path = "getmynames/{email}")
	public List<Davenfor> getSubmitterDavenfors(@PathVariable String email) throws ObjectNotFoundException {
		return submitterService.getAllSubmitterDavenfors(email);
	}
	
	//TODO: REMOVE IF UNNECESSARY, CONFIGURING EMAIL
	@PostMapping("testemail")
	public boolean sendTestEmail() throws MessagingException, EmailException {
		return emailSender.sendEmail("custom subject", "hopefully this will go through", "aliza.shanet@gmail.com", null, null, null);
	}

	@PostMapping(path = "{email}")
	public Davenfor addDavenfor(@RequestBody Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, EmailException, MessagingException {
		return submitterService.addDavenfor(davenfor, email);
	}

	@PutMapping(path = "updatename/{email}")
	public Davenfor updateDavenfor(@RequestBody @Valid Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, EmailException, PermissionException, MessagingException {
		return submitterService.updateDavenfor(davenfor, email, false);
	}

	@RequestMapping("extend/{davenforId}")
	public void extendDavenfor(@PathVariable long davenforId, @RequestParam("email") String email)
			throws ObjectNotFoundException, PermissionException, EmptyInformationException {
		submitterService.extendDavenfor(davenforId, email);
	}

	@DeleteMapping("delete/{id}/{email}") 
	public List<Davenfor> deleteDavenfor(@PathVariable long id, @PathVariable("email") String email)
			throws ObjectNotFoundException, PermissionException {
		return submitterService.deleteDavenfor(id, email);
	}

	@RequestMapping(path = "categories")
	public List<Category> findAllCategories() {
		return submitterService.getAllCategories();
	}

	@RequestMapping(path = "category/{id}")
	public Category findCategory(@PathVariable long id) throws ObjectNotFoundException {
		return submitterService.getCategory(id);
	}

}