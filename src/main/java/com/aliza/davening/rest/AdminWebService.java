package com.aliza.davening.rest;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davener;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.itextpdf.text.DocumentException;

import exceptions.DatabaseException;
import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.NoRelatedEmailException;
import exceptions.ObjectNotFoundException;

@RestController
@RequestMapping("admin")

public class AdminWebService {

	@Autowired
	AdminService adminService;

	@Autowired
	EmailSender emailSender;

	@PostMapping(path = "new")
	public boolean setAdmin(@RequestBody Admin admin) throws DatabaseException, EmptyInformationException {
		adminService.setAdmin(admin); 
		return true;
	}

	@PutMapping(path = "update")
	public boolean updateAdminSettings(@RequestBody Admin admin)
			throws DatabaseException, ObjectNotFoundException, EmptyInformationException {
		adminService.updateAdmin(admin);
		return true;
	}

	@RequestMapping(path = "davenfors")
	public List<Davenfor> findAllDavenfors() {
		return adminService.getAllDavenfors();
	}

	@RequestMapping(path = "daveners")
	public List<Davener> findAllDaveners() {
		return adminService.getAllDaveners();
	}

	@PostMapping(path = "davener")
	public String createDavener(@RequestBody Davener davener) throws NoRelatedEmailException {
		return adminService.addDavener(davener);
	}

	@PutMapping(path = "davener")
	public Davener updateDavener(@RequestBody Davener davener) throws ObjectNotFoundException, EmptyInformationException {
		return adminService.updateDavener(davener);
	}

	@DeleteMapping(path = "davener/{id}")
	public boolean deleteDavener(@PathVariable long id) throws ObjectNotFoundException {
		adminService.deleteDavener(id);
		return true;
	}

	@PostMapping(path = "weeklylist/{parashaId}")
	public boolean sendOutWeekly(@PathVariable long parashaId, @RequestBody String message)
			throws EmptyInformationException, IOException, MessagingException, EmailException, DocumentException,
			ObjectNotFoundException, DatabaseException {
		Parasha parasha = Utilities.findParasha(parashaId);
		emailSender.sendOutWeekly(parasha, message);
		return true;
	}
	
	//A simplified sendOutWeekly which takes a GET request (for the one sent through Admin's email link)
	@RequestMapping(path = "weeklylist/{parashaId}")
	public boolean sendOutWeeklyFromEmail(@PathVariable long parashaId)
			throws EmptyInformationException, IOException, MessagingException, EmailException, DocumentException,
			ObjectNotFoundException, DatabaseException {
		Parasha parasha = Utilities.findParasha(parashaId);
		emailSender.sendOutWeekly(parasha, null);
		return true;
	}

	@PostMapping(path = "urgent")
	public boolean sendOutUrgent(@RequestBody Davenfor davenfor) throws EmailException, EmptyInformationException {
		emailSender.sendUrgentEmail(davenfor);
		return true;
	}

	@RequestMapping(path = "categories")
	public List<Category> findAllCategories() {
		return adminService.getAllCategories();
	}

}
