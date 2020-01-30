package com.aliza.davening.rest;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.Utilities;
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
	
	@RequestMapping(path = "davenfors")
	public List<Davenfor> findAllDavenfors() {
		return adminService.getAllDavenfors();
	}
	
	@RequestMapping(path = "daveners")
	public List<Davener> findAllDaveners() {
		return adminService.getAllDaveners();
	}
	
	@PostMapping(path = "newdavener")
	public String createDavener(@RequestBody Davener davener) throws NoRelatedEmailException  {
		return adminService.addDavener(davener);	
	}
	
	@DeleteMapping(path = "deletedavener/{id}")
	public boolean deleteDavener(@PathVariable long id) throws ObjectNotFoundException{
		adminService.deleteDavener(id);
		return true;
	}
	
	@PostMapping(path = "weeklylist/{parashaId}")
	public boolean sendOutWeekly(@PathVariable long parashaId, @RequestBody String message) throws EmptyInformationException, IOException, MessagingException, EmailException, DocumentException, ObjectNotFoundException, DatabaseException {
		Parasha parasha = Utilities.findParasha(parashaId);
		emailSender.sendOutWeekly(parasha, message);
		return true;
	}

	
	
	
	
	
	
			
}
