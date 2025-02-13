package com.aliza.davening.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailScheme;
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

@Service
public class UserService {

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

		// TODO*: in future, adjust that admin can choose:
		// if (getMyGroupSettings(adminId).isNewNamePrompt()) {
		String subject = EmailScheme.getInformAdminOfNewNameSubject();
		String message = String.format(EmailScheme.getInformAdminOfNewName(), davenfor.getNameEnglish(),
				davenfor.getNameHebrew(), category.getCname().toString(), userEmail);
		// TODO*: include test
		emailSender.informAdmin(subject, message);
		//TODONOW: add link for admin to log into website
		// }

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
		String subject = EmailScheme.getInformAdminOfUpdateSubject();
		String message = String.format(EmailScheme.getInformAdminOfUpdate(), davenforToUpdate.getUserEmail(),
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
	public List<Davenfor> deleteDavenfor(long davenforId, String submitterEmail)
			throws ObjectNotFoundException, PermissionException {
		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor davenforToDelete = optionalDavenfor.get();
		String email = submitterEmail.trim();
		if (davenforToDelete.getUserEmail().equalsIgnoreCase(email)) {
			davenforRepository.delete(davenforToDelete);
		} else {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it.");
		}

		return davenforRepository.findAllDavenforByUserEmail(submitterEmail);
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

}
