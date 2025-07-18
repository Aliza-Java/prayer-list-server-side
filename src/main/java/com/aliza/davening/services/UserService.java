package com.aliza.davening.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.aliza.davening.util_classes.CategoryComparator;

@Service
@Transactional
public class UserService {

	public final String client = SchemeValues.client;

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
	public List<Davenfor> getAllUserDavenfors(String email) throws ObjectNotFoundException {

		
		// differentiating between non-existing email (this if) and empty list (which
		// will return fine and will be discerned)
		if (userRepository.findByEmail(email).isEmpty()){
			throw new ObjectNotFoundException("User with email " + email);
		}
		List<Davenfor> allUserDavenfors = davenforRepository.findAllDavenforByUserEmail(email);
		Collections.sort(allUserDavenfors, new CategoryComparator());
		return allUserDavenfors;
	}

	// tested
	public boolean addDavenfor(Davenfor davenfor, String userEmail)
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

		if (Category.isBanim(davenfor.getCategory())) {
			if (davenfor.noSpouseInfo()) {
				String message = "Banim category requires also a spouse name to be submitted. ";
				throw new EmptyInformationException(message);
			} else {
				if (davenfor.getNameEnglishSpouse() != null)
					davenfor.setNameEnglishSpouse(davenfor.getNameEnglishSpouse().trim());
				if (davenfor.getNameHebrewSpouse() != null)
					davenfor.setNameHebrewSpouse(davenfor.getNameHebrewSpouse().trim());
			}
		}

		davenfor.setUserEmail(existingOrNewUser(userEmail));

		davenfor.setCreatedAt(LocalDateTime.now());
		davenfor.setConfirmedAt(LocalDateTime.now());

		davenfor.setDeletedAt(null);

		Davenfor savedDavenfor;
		try {
			savedDavenfor = davenforRepository.save(davenfor);
		} catch (Exception e) {
			System.err.println("Error saving entity: " + e.getMessage());
			return false;
		}

		emailSender.sendConfirmationEmail(savedDavenfor.getId());

		// TODO*: in future, adjust that admin can choose if to get prompts: if
		// (getMyGroupSettings(adminId).isNewNamePrompt())...
		String subject;
		String message;

		String name = davenfor.getNameEnglish().isEmpty() ? davenfor.getNameHebrew() : davenfor.getNameEnglish();

		//something is empty
		if (davenfor.getNameEnglish().isEmpty() || davenfor.getNameHebrew().isEmpty()
				|| (Category.isBanim(davenfor.getCategory())
						&& (davenfor.getNameEnglishSpouse().isEmpty() || davenfor.getNameHebrewSpouse().isEmpty()))) {
			subject = String.format(EmailScheme.informAdminOfPartialNewNameSubject, name);
			message = EmailScheme.setAdminAlertMessage(true,  davenfor, client + "/admin");
		}

		else {
			subject = String.format(EmailScheme.informAdminOfNewNameSubject, name);
			message = String.format(EmailScheme.informAdminOfNewName, davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), category.getCname().getVisual(), userEmail, client + "/admin");
		}
		// TODO*: include test
		emailSender.informAdmin(subject, message);

		return true;
	}

	// tested
	@Transactional
	public Davenfor updateDavenfor(Davenfor updatedInfo, String submitterEmail, boolean isAdmin)
			throws EmptyInformationException, ObjectNotFoundException, PermissionException {

		if (updatedInfo == null) {
			throw new EmptyInformationException("No information submitted regarding the name you wish to update. ");
		}

		// Extracting id since it may be used more than once.
		long id = updatedInfo.getId();

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

		// before changes are saved in the DB, comparing old and new to see if anything
		// was erased (and inform admin)
		Davenfor existingInfo = optionalDavenfor.get();
		boolean infoRemoved = false;
		String subject = "";
		String message = "";

		// informing admin if edited name erased an important piece of the name
		if ((existingInfo.getNameEnglish().length() > 0 && updatedInfo.getNameEnglish().length() == 0)
				|| (existingInfo.getNameHebrew().length() > 0 && updatedInfo.getNameHebrew().length() == 0)
				|| (Category.isBanim(updatedInfo.getCategory()) && existingInfo.getNameEnglishSpouse().length() > 0
						&& updatedInfo.getNameEnglishSpouse().length() == 0)
				|| (Category.isBanim(updatedInfo.getCategory()) && existingInfo.getNameHebrewSpouse().length() > 0
						&& updatedInfo.getNameHebrewSpouse().length() == 0)) {
			infoRemoved = true;
			String name = updatedInfo.getNameEnglish().isEmpty() ? updatedInfo.getNameHebrew() : updatedInfo.getNameEnglish();
			subject = String.format(EmailScheme.informAdminOfPartialEditNameSubject, name);
			message = EmailScheme.setAdminAlertMessage(false, updatedInfo, client + "/admin");
		}

		// Trim all names nicely
		existingInfo.setNameEnglish(updatedInfo.getNameEnglish().trim());
		existingInfo.setNameHebrew(updatedInfo.getNameHebrew().trim());
		if (existingInfo.getCategory() != updatedInfo.getCategory())
			existingInfo.setCategory(updatedInfo.getCategory());

		// If davenfor needs 2 names (e.g. banim), validate that second name is in
		// too, and if indeed exist - trim them.
		if (Category.isBanim(updatedInfo.getCategory())) {
			if (updatedInfo.noSpouseInfo()) {
				String errorMessage = "Banim category requires also a spouse name to be submitted. ";
				throw new EmptyInformationException(errorMessage);
			} else {
				if (updatedInfo.getNameEnglishSpouse() != null)
					existingInfo.setNameEnglishSpouse(updatedInfo.getNameEnglishSpouse().trim());
				if (updatedInfo.getNameHebrewSpouse() != null)
					existingInfo.setNameHebrewSpouse(updatedInfo.getNameHebrewSpouse().trim());
			}
		}

		if (!isAdmin) {
			existingInfo.setUserEmail(existingOrNewUser(submitterEmail));
		}
		existingInfo.setUpdatedAt(LocalDateTime.now());
		existingInfo.setConfirmedAt(LocalDateTime.now());

		// Davenfor will expire in future according to it's category's settings.
		// Category categoryObj = Category.getCategory(davenforToUpdate.getCategory());
		// davenforToUpdate.setExpireAt(LocalDate.now().plusDays(categoryObj.getUpdateRate()));

		davenforRepository.save(existingInfo);
		entityManager.flush();
		entityManager.clear();

		// for now I don't think it's necessary to inform admin on every update
		// if (getMyGroupSettings(adminId).isNewNamePrompt()) {
//		String subject = EmailScheme.informAdminOfUpdateSubject;
//		String message = String.format(EmailScheme.informAdminOfUpdate, davenforToUpdate.getUserEmail(),
//				davenforToUpdate.getNameEnglish(), davenforToUpdate.getNameHebrew(), davenforToUpdate.getCategory());

		if (infoRemoved)
			emailSender.informAdmin(subject, message);

		return existingInfo;
	}

	// TODO* need to test after adjustments
	public Davenfor extendDavenfor(long davenforId, String token)
			throws ObjectNotFoundException, PermissionException, EmptyInformationException {

		if (token == null) {
			throw new EmptyInformationException("No associated email address was received. ");
		}

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor davenforToExtend = optionalDavenfor.get();

		// todo* in future - validity checks on email. How long is it valid for?
		String email = jwtUtils.extractEmailFromToken(token);

		if (!davenforToExtend.getUserEmail().equalsIgnoreCase(email)) {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it.");
		}

		// Extending the davenfor's expiration date according to the defined length in
		// its category.
		// Category categoryObj = Category.getCategory(davenforToExtend.getCategory());
		// LocalDate extendedDate =
		// LocalDate.now().plusDays(categoryObj.getUpdateRate());
		// davenforRepository.extendExpiryDate(davenforId, extendedDate,
		// LocalDate.now());

		if (davenforToExtend.wasDeleted())
			davenforRepository.reviveDavenfor(davenforId);

		davenforRepository.setConfirmedAt(LocalDateTime.now(), davenforId);

		return davenforToExtend;
	}

	// tested
	public List<Davenfor> deleteDavenfor(long davenforId, String auth, boolean viaEmail)
			throws ObjectNotFoundException, PermissionException {
		Optional<Davenfor> optionalDavenfor = davenforRepository.findByIdIncludingDeleted(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor davenforToDelete = optionalDavenfor.get();
		String email = viaEmail ? jwtUtils.extractEmailFromToken(auth) : auth;
		if (davenforToDelete.getUserEmail().equalsIgnoreCase(email)) {
			davenforRepository.softDeleteById(davenforToDelete.getId());
		} else {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it.");
		}

		String name = davenforToDelete.getNameEnglish().trim().length() == 0 ? davenforToDelete.getNameHebrew() : davenforToDelete.getNameEnglish();
		String adminSubject = String.format(EmailScheme.deleteNameAdminSubject, name);
		String adminMessage = String.format(EmailScheme.deleteNameAdminMessage, name,
				davenforToDelete.getCategory(), davenforToDelete.getUserEmail());
		emailSender.informAdmin(adminSubject, adminMessage);

		if (viaEmail)
			return List.of(davenforToDelete); // return one so that can extract it in the confirmation message
		else {
			List<Davenfor> allUserDavenfors = davenforRepository.findAllDavenforByUserEmail(email);
			Collections.sort(allUserDavenfors, new CategoryComparator());
			return allUserDavenfors;// return all to show on website remaining ones
		}
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
	// similar to admin's deactivate
	public String unsubscribe(String token) throws EmptyInformationException {
		String email = jwtUtils.extractEmailFromToken(token);
		Optional<User> userToUnsubscribe = userRepository.findByEmail(email);
		if (userToUnsubscribe.isEmpty()) {
			System.out.println(String.format(
					"The email cannot be deactivated because it is not found: %s.  Please check the email address. ",
					email));
			return "There was a problem unsubscribing the email sent";
		}
		if (!userToUnsubscribe.get().isActive()) { // Just to log/notify, and continue business as usual
			System.out.println(String
					.format("The email %s has already been deactivated from receiving the Davening lists. ", email));
		} else {
			userRepository.deactivateUser(email);
			entityManager.flush();
			entityManager.clear();
		}
		emailSender.notifydeactivatedUser(email);
		String response = String.format(SchemeValues.unsubscribeText, email);
		return response;
	}
	
	public boolean setNewOtp(String email) throws EmailException, ObjectNotFoundException {
		Optional<User> optionalUser = userRepository.findByEmail(email);

	    if (optionalUser.isEmpty()) {
	        throw new ObjectNotFoundException("user with email " + email);
	    }
		
		String otp = jwtUtils.generateOtp();

	    int rowsUpdated = userRepository.setOtp(otp, email);
	    System.out.println("Rows updated: " + rowsUpdated);

	    return emailSender.sendOtp(email, otp); // Send email with code
	}
	
	public boolean verifyOtp(String email, String otp) throws PermissionException {
	    // Retrieve the OTP from DB (and check expiration, e.g. 5 min)
	    Optional<User> optionalUser = userRepository.findByEmail(email);
	    	    
	    if (!otp.equals(optionalUser.get().getOtp())) {
	    	throw new PermissionException("Invalid or expired code");
	    }

	    userRepository.setOtp("", email);

	    return true;
	}
	
}
