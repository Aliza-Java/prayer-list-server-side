package com.aliza.davening.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.EmailScheme;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.User;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.UserRepository;
import com.aliza.davening.security.JwtUtils;

@Service
@Transactional
public class UserService {

	@Value("${client.origin}")
	String client;

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AdminRepository adminRepository;

	@Autowired
	EmailSender emailSender;

	@Autowired
	EntityManager entityManager;

	@Autowired
	private JwtUtils jwtUtils;

	// All user functions receive his email address and allow him to proceed if
	// his email matches davenfor.getUser().getEmail()

//	@Value("${admin.id}")
	public long adminId = 1;

	// TODO*: In future, make option to add more groups and run each individually
//	private Admin getMyGroupSettings(long adminId) throws ObjectNotFoundException {
//
//		Optional<Admin> groupSettings = adminRepository.findById(adminId);
//		if (!groupSettings.isPresent()) {
//			throw new ObjectNotFoundException("Davening group with id " + adminId);
//		}
//		return groupSettings.get();
//
//	}

	// According to email address user can see all names he submitted.
	// tested
	public List<Davenfor> getAllUserDavenfors(String email) {

		// differentiating between non-existing email (this if) and empty list (which
		// will return fine and will be discerned)
		if (userRepository.findByEmail(email) == null) {
			return new ArrayList<Davenfor>();
		}
		return davenforRepository.findAllDavenforByUserEmail(email);
	}

	// tested
	public Davenfor addDavenfor(Davenfor davenfor, String userEmail)
			throws EmptyInformationException, EmailException, IOException, ObjectNotFoundException {

		Category category = Category.getCategory(davenfor.getCategory());
		if (category == null) {
			throw new EmptyInformationException("Category not found");
		}

		// Trim all names nicely
		davenfor.setNameEnglish(davenfor.getNameEnglish().trim());
		davenfor.setNameHebrew(davenfor.getNameHebrew().trim());

		// If davenfor needs 2 names (e.g. Zera shel Kayama), validate that second name
		// is in too, and if indeed exist - trim them.

		if (Category.isBanim(category.getCname().toString())) {
			if (davenfor.noSpouseInfo()) {
				throw new EmptyInformationException(
						"This category requires also a spouse name (English and Hebrew) to be submitted. ");
			} else {
				davenfor.setNameEnglishSpouse(davenfor.getNameEnglishSpouse().trim());
				davenfor.setNameHebrewSpouse(davenfor.getNameHebrewSpouse().trim());
			}
		}

		davenfor.setUserEmail(existingOrNewUser(userEmail));

		davenfor.setCreatedAt(LocalDate.now());
		davenfor.setLastConfirmedAt(LocalDate.now());

		// Davenfor will expire in future according to its category's settings.
		davenfor.setExpireAt(LocalDate.now().plusDays(category.getUpdateRate()));

		Davenfor savedDavenfor;
		try {
			savedDavenfor = davenforRepository.save(davenfor);
		} catch (Exception e) {
			System.err.println("Error saving entity: " + e.getMessage());
			return null;
		}

		emailSender.sendConfirmationEmail(savedDavenfor.getId());

		// TODO*: in future, adjust that admin can choose if to get prompts: if
		// (getMyGroupSettings(adminId).isNewNamePrompt())...
		String subject = EmailScheme.informAdminOfNewNameSubject;
		String message = String.format(EmailScheme.informAdminOfNewName, davenfor.getNameEnglish(),
				davenfor.getNameHebrew(), category.getCname().toString(), userEmail, client + "/admin");
		// TODO*: include test
		emailSender.informAdmin(subject, message);

		return savedDavenfor;
	}

	// tested
	public Davenfor updateDavenfor(Davenfor davenforToUpdate, String submitterEmail, boolean isAdmin)
			throws EmptyInformationException, ObjectNotFoundException, PermissionException {

		if (davenforToUpdate == null) {
			throw new EmptyInformationException("No information submitted regarding the name you wish to update. ");
		}

		// Extracting id since it may be used more than once.
		long id = davenforToUpdate.getId();

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(id);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + id);
		}

		if (!isAdmin) {
			// Comparing email with davenfor-submitter from Database, since the davenfor
			// coming in may have empty email and lead to null pointer exception.
			if (!optionalDavenfor.get().getUserEmail().equalsIgnoreCase(submitterEmail)) {
				throw new PermissionException(
						"This name is registered under a different email address.  You do not have the permission to update it.");
			}
		}

		// Trim all names nicely
		davenforToUpdate.setNameEnglish(davenforToUpdate.getNameEnglish().trim());
		davenforToUpdate.setNameHebrew(davenforToUpdate.getNameHebrew().trim());

		// If davenfor needs 2 names (e.g. banim), validate that second name is in
		// too, and if indeed exist - trim them.
		if (Category.isBanim(davenforToUpdate.getCategory())) {
			if (davenforToUpdate.noSpouseInfo()) {
				throw new EmptyInformationException(
						"This category requires also a spouse name (English and Hebrew) to be submitted. ");
			} else {
				davenforToUpdate.setNameEnglishSpouse(davenforToUpdate.getNameEnglishSpouse().trim());
				davenforToUpdate.setNameHebrewSpouse(davenforToUpdate.getNameHebrewSpouse().trim());
			}
		}

		if (!isAdmin) {
			davenforToUpdate.setUserEmail(existingOrNewUser(submitterEmail));
		}
		davenforToUpdate.setUpdatedAt(LocalDate.now());
		davenforToUpdate.setLastConfirmedAt(LocalDate.now());

		// Davenfor will expire in future according to it's category's settings.
		Category categoryObj = Category.getCategory(davenforToUpdate.getCategory());
		davenforToUpdate.setExpireAt(LocalDate.now().plusDays(categoryObj.getUpdateRate()));

		davenforRepository.save(davenforToUpdate);

		// if (getMyGroupSettings(adminId).isNewNamePrompt()) {
		String subject = EmailScheme.informAdminOfUpdateSubject;
		String message = String.format(EmailScheme.informAdminOfUpdate, davenforToUpdate.getUserEmail(),
				davenforToUpdate.getNameEnglish(), davenforToUpdate.getNameHebrew(), davenforToUpdate.getCategory());
		emailSender.informAdmin(subject, message);
		// }

		return davenforToUpdate;
	}

	// tested
	public boolean extendDavenfor(long davenforId, String submitterEmail)
			throws ObjectNotFoundException, PermissionException, EmptyInformationException {

		if (submitterEmail == null) {
			throw new EmptyInformationException("No associated email address was received. ");
		}

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor davenforToExtend = optionalDavenfor.get();

		if (!davenforToExtend.getUserEmail().equalsIgnoreCase(submitterEmail)) {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it.");
		}

		// Extending the davenfor's expiration date according to the defined length in
		// its category.
		Category categoryObj = Category.getCategory(davenforToExtend.getCategory());
		LocalDate extendedDate = LocalDate.now().plusDays(categoryObj.getUpdateRate());
		davenforRepository.extendExpiryDate(davenforId, extendedDate, LocalDate.now());
		return true;
	}

	// tested
	public List<Davenfor> deleteDavenfor(long davenforId, String auth, boolean viaEmail)
			throws ObjectNotFoundException, PermissionException {
		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor davenforToDelete = optionalDavenfor.get();
		String email = viaEmail? jwtUtils.getUserNameFromJwtToken(auth) : auth;
		if (davenforToDelete.getUserEmail().equalsIgnoreCase(email)) {
			davenforRepository.delete(davenforToDelete);
		} else {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it.");
		}
		
		String adminSubject = String.format(EmailScheme.deleteNameSubject, davenforToDelete.getNameEnglish());
		String adminMessage = String.format(EmailScheme.deleteNameMessage, davenforToDelete.getNameEnglish(), davenforToDelete.getCategory(), davenforToDelete.getUserEmail());
		emailSender.informAdmin(adminSubject, adminMessage);

		if (viaEmail)
			return List.of(davenforToDelete); //return one so that can extract it in the confirmation message
		else
			return davenforRepository.findAllDavenforByUserEmail(email); //return all to show on website remaining ones		
	}

	// tested
	public List<Category> getAllCategories() throws ObjectNotFoundException {
		List<Category> categories = categoryRepository.findAllOrderById();
		if (categories.size() < 1)
			throw new ObjectNotFoundException("System categories");
		return categories;
	}

	// tested
	public String existingOrNewUser(String userEmail) {
		Optional<User> validUser = userRepository.findByEmail(userEmail);

		// If user has never submitted a name, need to create a new one in
		// database.
		if (validUser.isEmpty()) {
			userRepository.save(new User(userEmail));
		}
		return userEmail;
	}

	// tested
	public Category getCategory(long id) throws ObjectNotFoundException {
		Optional<Category> optionalCategory = categoryRepository.findById(id);

		if (!optionalCategory.isPresent()) {
			throw new ObjectNotFoundException("Category with id " + id);
		}

		return optionalCategory.get();
	}

	// TODO* test
	// similar to admin's disactivate
	public String unsubscribe(String token) throws EmptyInformationException {
		String email = jwtUtils.getUserNameFromJwtToken(token);
		Optional<User> userToUnsubscribe = userRepository.findByEmail(email);
		if (userToUnsubscribe.isEmpty()) {
			System.out.println(String.format(
					"The email cannot be disactivated because it is not found: %s.  Please check the email address. ",
					email));
			return "There was a problem unsubscribing the email sent";
		}
		if (!userToUnsubscribe.get().isActive()) { // Just to log/notify, and continue business as usual
			System.out.println(String
					.format("The email %s has already been disactivated from receiving the davening lists. ", email));
		} else {
			userRepository.disactivateUser(email);
			entityManager.flush();
			entityManager.clear();
		}
		emailSender.notifyDisactivatedUser(email);
		String response = String.format(SchemeValues.unsubscribeText, email);
		return response;
	}
}
