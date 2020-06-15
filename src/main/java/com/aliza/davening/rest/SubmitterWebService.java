package com.aliza.davening.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.services.SubmitterService;

import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.ObjectNotFoundException;
import exceptions.PermissionException;

@RestController
public class SubmitterWebService {

	@Autowired
	SubmitterService submitterService;

	@RequestMapping(path = "getmynames/{email}")
	public List<Davenfor> getSubmitterDavenfors(@PathVariable String email) throws ObjectNotFoundException {
		return submitterService.getAllSubmitterDavenfors(email);
	}

	@PostMapping(path = "{email}")
	public Davenfor addDavenfor(@RequestBody Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, EmailException {
		return submitterService.addDavenfor(davenfor, email);
	}

	@PutMapping(path = "updatename/{email}")
	public Davenfor updateDavenfor(@RequestBody @Valid Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, EmailException, PermissionException {
			return submitterService.updateDavenfor(davenfor, email);
	}

	@RequestMapping("extend/{davenforId}")
	public void extendDavenfor(@PathVariable long davenforId, @RequestParam("email") String email)
			throws ObjectNotFoundException, PermissionException, EmptyInformationException {
		submitterService.extendDavenfor(davenforId, email);
	}
	
	@RequestMapping("delete/{id}")
	public void deleteDavenfor(@PathVariable long id, @RequestParam("email") String email)
			throws ObjectNotFoundException, PermissionException {
		submitterService.deleteDavenfor(id, email);
	}
}
