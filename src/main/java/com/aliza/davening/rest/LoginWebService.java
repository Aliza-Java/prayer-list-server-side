package com.aliza.davening.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.services.AdminService;

import exceptions.LoginException;

@RestController
public class LoginWebService {

	@Autowired
	AdminService adminService;

	@Autowired
	HttpServletRequest request;

	Admin admin = null;

	//Creating as GET request since the email link also turns to it (and is GET by default)
	@RequestMapping("login")
	public Admin login(@RequestBody Admin login) throws LoginException {

		if (adminService.login(login.getEmail(), login.getPassword())!=null) {
			HttpSession session = request.getSession();
			
			//TODO: consider if need both of these.  Or if adminService saves it from the login. 
			session.setAttribute("currentAdmin", login);
			admin = login;
		} else // login did not go through
		{
			throw new LoginException("The credentials you provided do not match.");
		}
		//TODO: maybe return boolean?  he already has admin. 
		return admin;
	}

	@PostMapping("logout")
	public void logout() throws LoginException {
		try {
			request.getSession(false).invalidate();
		} catch (Throwable e) {
			throw new LoginException("Ok, you're logged out.  But it didn't go so smoothly for some reason.");
		}

	}
}
