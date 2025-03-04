package com.aliza.davening.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.services.UserService;

@Controller // special web service for all methods that redirect to a new response page.
			// @Controller (vs. @RestController) - vital for Thymeleaf
@RequestMapping("direct")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")
//creating separate web controller to allow annotating it 'controller' where it can render a page instead of returning a response
public class PopupWebService {

	public final String client = SchemeValues.client;

	@Autowired
	UserService userService;

	// TODO*: test
	@GetMapping(path = "unsubscribe/{token}")
	public String unsubscribe(@PathVariable String token, Model model) throws EmptyInformationException {
		String response = userService.unsubscribe(token);
		model.addAttribute("response", response);
		model.addAttribute("client", client);
		return "unsubscribe-confirmation"; // maps to `src/main/resources/templates/unsubscribe-confirmation.html`due to
											// Thymeleaf
	}

	// todo*: test sending via token, with errors and good
	@DeleteMapping("delete/{id}/{token}") 
	public String deleteDavenforViaEmail(@PathVariable long id, @PathVariable String token, Model model) {
		Davenfor deletedDf;
		model.addAttribute("client", client);

		try {
			deletedDf = userService.deleteDavenfor(id, token, true).get(0);
		} catch (Exception e) {
			e.printStackTrace();

			return "delete-problem"; // maps to `src/main/resources/templates/delete-problem.html` due to Thymeleaf
		}

		model.addAttribute("response", String.format("The name %s has been removed from our lists", deletedDf.getNameEnglish()));
		return "delete-confirmation"; // maps to `src/main/resources/templates/delete-confirmation.html` due to
										// Thymeleaf
	}
}