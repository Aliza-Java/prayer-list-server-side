package com.aliza.davening.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.security.TokenCheck;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.UserService;

import io.jsonwebtoken.ExpiredJwtException;

@Controller // special web service for all methods that redirect to a new response page.
			// @Controller (vs. @RestController) - vital for Thymeleaf
@RequestMapping("direct")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")
//creating separate web controller to allow annotating it 'controller' where it can render a page instead of returning a response
public class PopupWebService {

	public final String client = SchemeValues.client;

	@Autowired
	UserService userService;

	@Autowired
	AdminService adminService;

	@Autowired
	EmailSender emailSender;

	@Autowired
	JwtUtils jwtUtils;

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
		} catch (ObjectNotFoundException e) {
			model.addAttribute("status", "This name may have been deleted already");
			model.addAttribute("action", "Take me to the website");
			return "delete-problem";
		} catch (Exception e) {
			model.addAttribute("status", "There was a problem deleting this name");
			model.addAttribute("action", "Delete directly from the website");
			return "delete-problem"; // maps to `src/main/resources/templates/delete-problem.html` due to Thymeleaf
		}

		model.addAttribute("response",
				String.format("The name %s has been removed from our lists", deletedDf.getNameEnglish()));
		return "delete-confirmation"; // maps to `src/main/resources/templates/delete-confirmation.html` due to
										// Thymeleaf
	}

	// to test
	@GetMapping("extend/{id}/{token}")
	public String extendDavenfor(@PathVariable long id, @PathVariable String token, Model model) {

		Davenfor extendedDf = null;

		model.addAttribute("client", client);

		try {
			extendedDf = userService.extendDavenfor(id, token);
		} catch (Exception e) {
			model.addAttribute("message", "There was a problem confirming this name");
			model.addAttribute("reason", e.getMessage());
			return "extend-problem"; // maps to `src/main/resources/templates/extend-problem.html` due to Thymeleaf
		}

		model.addAttribute("response", String.format("Thank you for confirming %s in the category: %s",
				extendedDf.getNameEnglish(), extendedDf.getCategory()));
		return "extend-confirmation"; // maps to `src/main/resources/templates/extend-confirmation.html` due to
										// Thymeleaf

	}

	// to test
	@PostMapping("preview")
	@ResponseBody
	public String producePreview(@RequestBody TokenCheck data)
			throws ObjectNotFoundException, EmptyInformationException {

		try {
			boolean tokenValid = adminService.checkTokenForDirect(data.getToken(), data.getEmail());
			if (!tokenValid)
				return "There was a problem generating the preview";
			return adminService.previewWeekly(null);

		} catch (ExpiredJwtException e) {
			System.out.println("User used an expired email. " + e.getMessage());
			return "The email link appears to be expired.  Please use a recent email or log into the website.";
		}
	}

	// to test
	@PostMapping("send")
	@ResponseBody
	public ResponseEntity<Map<String, String>> produceSend(@RequestBody TokenCheck data)
			throws ObjectNotFoundException {
		try {
			boolean tokenValid = adminService.checkTokenForDirect(data.getToken(), data.getEmail());
			if (!tokenValid)
				return new ResponseEntity<>(Map.of("message", "The token didn't match the email"),
						HttpStatus.FORBIDDEN);
		} catch (ExpiredJwtException e) {
			System.out.println("User used an expired email. " + e.getMessage());
			return new ResponseEntity<>(
					Map.of("message",
							"This email link appears to be expired.  Please use a recent email or log into the website."),
					HttpStatus.FORBIDDEN);
		}

		if (!adminService.checkPassword(data.getPassword(), data.getEmail())) {
			// purposefully not 'UNAUTHORIZED' because that navigates to guest automatically
			return new ResponseEntity<>(Map.of("message", "The password is incorrect"), HttpStatus.FORBIDDEN);
		}
		try {
			emailSender.sendOutWeekly(null);
		} catch (EmailException | EmptyInformationException e) {
			System.out.println("There was an error sending out the list: ");
			e.printStackTrace();
			return new ResponseEntity<>(
					Map.of("message", "There was an error sending out the list.  Please contact your website admin"),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(Map.of("message", "The list has been sent"), HttpStatus.OK);
	}
}