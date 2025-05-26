package com.aliza.davening.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.entities.User;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.NoRelatedEmailException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.UserService;
import com.aliza.davening.util_classes.AdminSettings;
import com.aliza.davening.util_classes.Password;
import com.aliza.davening.util_classes.Weekly;

@RestController
@RequestMapping("admin")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")
public class AdminWebService {

	@Autowired
	AdminService adminService;

	@Autowired
	EmailSender emailSender;

	@Autowired
	UserService userService;

	@Value("${admin.id}")
	long adminId;

	// tested
	@PutMapping(path = "update")
	public boolean updateAdminSettings(@RequestBody AdminSettings settings) throws ObjectNotFoundException {
		adminService.updateAdmin(settings);
		return true;
	}

	// tested
	@RequestMapping("settings/{email}")
	public AdminSettings getAdminSettings(@PathVariable String email) throws ObjectNotFoundException {
		return adminService.getAdminSettings(email);
	}

	// tested
	@PostMapping("checkpass/{email}")
	// saved Password as its own object, to allow passing in request body
	public boolean checkPassword(@RequestBody Password password, @PathVariable String email)
			throws ObjectNotFoundException {
		return adminService.checkPassword(password.getPassword(), email);
	}

	// tested
	@RequestMapping("davenfors")
	public List<Davenfor> findAllDavenfors() {
		return adminService.getAllDavenfors();
	}

	// tested
	@DeleteMapping("delete/{id}")
	public List<Davenfor> deleteDavenfor(@PathVariable long id) throws ObjectNotFoundException {
		return adminService.deleteDavenfor(id);
	}

	// tested
	@RequestMapping("users")
	public List<User> findAllUsers() {
		return adminService.getAllUsers();
	}

	// tested
	@PostMapping(path = "user")
	public List<User> createUser(@RequestBody User user) throws NoRelatedEmailException {
		return adminService.addUser(user);
	}

	// tested
	@PutMapping(path = "user")
	public List<User> updateUser(@RequestBody User user) throws ObjectNotFoundException {
		return adminService.updateUser(user);
	}

	// tested
	@DeleteMapping(path = "user/{id}")
	public List<User> deleteUser(@PathVariable long id) throws ObjectNotFoundException {
		return adminService.deleteUser(id);
	}

	// tested
	@PostMapping(path = "disactivate/{userEmail}")
	public List<User> disactivateUser(@PathVariable String userEmail) throws EmptyInformationException {
		return adminService.disactivateUser(userEmail);
	}

	// tested
	@PostMapping(path = "activate/{userEmail}")
	public List<User> activateUser(@PathVariable String userEmail) throws EmptyInformationException {
		return adminService.activateUser(userEmail);
	}

	// tested.  TODO*: still test exceptions thrown
	@PostMapping(path = "weekly")
	public boolean sendOutWeekly(@RequestBody Weekly weeklyInfo) throws EmptyInformationException, ObjectNotFoundException, EmailException   {
		return emailSender.sendOutWeekly(weeklyInfo);		 
	}

	// tested
	@PostMapping(path = "preview", produces = "text/plain")
	public String previewWeekly(@RequestBody Weekly weeklyInfo)
			throws EmptyInformationException, ObjectNotFoundException {
		System.out.println("Generating preview for week: " + weeklyInfo.parashaNameEnglish);
		return adminService.previewWeekly(weeklyInfo);
	}

	// tested
	@PutMapping(path = "updatedavenfor")
	public List<Davenfor> updateNameByAdmin(@RequestBody Davenfor davenfor)
			throws EmptyInformationException, ObjectNotFoundException, PermissionException {
		userService.updateDavenfor(davenfor, null, true);
		return adminService.getAllDavenfors();
	}

	// tested
	@PutMapping(path = "updatename/{email}")
	// TODO*: add test for 'validity' of Davenfor
	public Davenfor updateDavenfor(@RequestBody @Valid Davenfor davenfor, @PathVariable String email)
			throws EmptyInformationException, ObjectNotFoundException, PermissionException {
		return userService.updateDavenfor(davenfor, email, false);
	}

	// tested
	@PostMapping(path = "urgent")
	public boolean sendOutUrgent(@RequestBody Davenfor davenfor) throws EmptyInformationException {
		emailSender.sendUrgentEmail(davenfor);
		return true;
	}

	// tested
	@RequestMapping(path = "categories")
	public List<Category> findAllCategories() {
		return adminService.getAllCategories();
	}

	// tested
	@RequestMapping(path = "parashot")
	public List<Parasha> findAllParashot() {
		return adminService.getAllParashot();
	}

	//TODO*: need to test
	@RequestMapping(path = "parasha")
	public Parasha getCurrentParasha() {
		return adminService.findCurrentParasha();
	}

	@RequestMapping(path = "category") // TODO*: test
	public Category getCurrentCategory() {
		return adminService.findCurrentCategory();
	}
}
