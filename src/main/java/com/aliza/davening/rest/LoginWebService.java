package com.aliza.davening.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.exceptions.LoginException;
import com.aliza.davening.services.AdminService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class LoginWebService {

	@Autowired
	AdminService adminService;

	@Autowired
	HttpServletRequest request;

	Admin admin = null;

	// Creating as GET request since the email link also turns to it (and is GET by
	// default)
	@PostMapping("login")
	public Admin login(@RequestBody Admin login) throws LoginException {

		Admin loginResult = adminService.login(login.getEmail(), login.getPassword());
		if (loginResult != null) {
			HttpSession session = request.getSession();

			session.setAttribute("currentAdmin", loginResult);
			admin = loginResult;
		} else // login did not go through
		{
			throw new LoginException("The credentials you provided do not match.");
		}
		return loginResult;
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
