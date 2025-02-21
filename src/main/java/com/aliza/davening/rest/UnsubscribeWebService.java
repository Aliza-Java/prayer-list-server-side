package com.aliza.davening.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.services.UserService;

@Controller
@RequestMapping("unsubscribe")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")
//creating separate web controller to allow annotating it 'controller' where it can render a page instead of returning a response
public class UnsubscribeWebService {

	@Value("${client.origin}")
	String client;

	@Autowired
	UserService userService;

	// TODO*: test
	@GetMapping(path = "{token}")
	public String unsubscribe(@PathVariable String token, Model model) throws EmptyInformationException {
		String response = userService.unsubscribe(token);
		model.addAttribute("response", response);
		model.addAttribute("client", client);
		return "unsubscribe-confirmation"; // This maps to `src/main/resources/templates/unsubscribe-confirmation.html` thanks to Thymeleaf
	}
}