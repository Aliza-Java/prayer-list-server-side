package com.aliza.davening.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailScheme;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.CategoryType;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;

@Service("submitterService")
public class SubmitterService {

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	SubmitterRepository submitterRepository;

	@Autowired
	AdminRepository adminRepository;

	@Autowired
	EmailSender emailSender;

	// All submitter functions receive his email address and allow him to proceed if
	// his email matches davenfor.getSubmitter().getEmail()

//	@Value("${admin.id}")
	public long adminId=1;
	
	//TODO: In future, make option to add more groups and run each individually
//	private Admin getMyGroupSettings(long adminId) throws ObjectNotFoundException {
//
//		Optional<Admin> groupSettings = adminRepository.findById(adminId);
//		if (!groupSettings.isPresent()) {
//			throw new ObjectNotFoundException("Davening group with id " + adminId);
//		}
//		return groupSettings.get();
//
//	}

	// According to email address submitter can see all names he submitted.
	public List<Davenfor> getAllSubmitterDavenfors(String email) throws ObjectNotFoundException {

		// differentiating between non-existing email (this if) and empty list (which
		// will return fine and will be discerned)
		if (submitterRepository.findByEmail(email) == null) {
			return new ArrayList<Davenfor>();
		}
		return davenforRepository.findAllDavenforBySubmitterEmail(email);
	}

	public Davenfor addDavenfor(Davenfor davenfor, String submitterEmail)
			throws EmptyInformationException, ObjectNotFoundException, EmailException, MessagingException {

		/*
		 * Ensuring there is a real category and associating it with the davenfor.
		 */
		//TODO - fix so that checks correctly that its a normal category.
//		Optional<Category> optionalCategory = categoryRepository.findById(davenfor.getCategory().getId());
//		if (!optionalCategory.isPresent()) {
//			throw new EmptyInformationException("No existing category chosen. ");
//		}

		// Trim all names nicely
		davenfor.setNameEnglish(davenfor.getNameEnglish().trim());
		davenfor.setNameHebrew(davenfor.getNameHebrew().trim());

		// If davenfor needs 2 names (e.g. Zera shel Kayama), validate that second name
		// is in too, and if indeed exist - trim them.

		if (CategoryType.BANIM.equals(davenfor.getCategory().getCname())) {
			if (davenfor.getNameEnglishSpouse() == null || davenfor.getNameHebrewSpouse() == null) {
				throw new EmptyInformationException(
						"This category requires also a spouse name (English and Hebrew) to be submitted. ");
			} else {
				davenfor.setNameEnglishSpouse(davenfor.getNameEnglishSpouse().trim());
				davenfor.setNameHebrewSpouse(davenfor.getNameHebrewSpouse().trim());

			}
		}

		davenfor.setSubmitterEmail(existingOrNewSubmitter(submitterEmail));

		davenfor.setCreatedAt(LocalDate.now());
		davenfor.setLastConfirmedAt(LocalDate.now());

		// Davenfor will expire in future according to its category's settings.
		davenfor.setExpireAt(LocalDate.now().plusDays(davenfor.getCategory().getUpdateRate()));

		davenforRepository.save(davenfor);

		//TODO: in future - allow email if admin wants. If admin's setting defines that admin should get an email upon new name being
		// added:
		//if (getMyGroupSettings(adminId).isNewNamePrompt()) {
			String subject = EmailScheme.getInformAdminOfNewNameSubject();
			String message = String.format(EmailScheme.getInformAdminOfNewName(), davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getCategory().getCname(), submitterEmail);
			emailSender.informAdmin(subject, message);
		//}

		return davenfor;

	}

	public Davenfor updateDavenfor(Davenfor davenforToUpdate, String submitterEmail, boolean isAdmin)
			throws EmptyInformationException, ObjectNotFoundException, EmailException, PermissionException, MessagingException {

		if (davenforToUpdate == null) {
			throw new EmptyInformationException("No information submitted regarding the name you wish to update. ");
		}

		// Extracting id since it may be used more than once.
		long id = davenforToUpdate.getId();

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(id);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + id);
		}

		if(!isAdmin) {
			// Comparing email with davenfor-submitter from Database, since the davenfor
			// coming in may have empty email and lead to null pointer exception.
			if (!optionalDavenfor.get().getSubmitterEmail().equalsIgnoreCase(submitterEmail)) {
				throw new PermissionException(
						"This name is registered under a different email address.  You do not have the permission to update it.");
			}
		}

		// Trim all names nicely
		davenforToUpdate.setNameEnglish(davenforToUpdate.getNameEnglish().trim());
		davenforToUpdate.setNameHebrew(davenforToUpdate.getNameHebrew().trim());

		// If davenfor needs 2 names (e.g. banim), validate that second name is in
		// too, and if indeed exist - trim them.
		//TODO - change this banim condition to 'isBanim' across the board
		if (CategoryType.BANIM.equals(davenforToUpdate.getCategory().getCname())) {
			if (davenforToUpdate.getNameEnglishSpouse() == null || davenforToUpdate.getNameHebrewSpouse() == null) {
				throw new EmptyInformationException(
						"This category requires also a spouse name (English and Hebrew) to be submitted. ");
			} else {
				davenforToUpdate.setNameEnglishSpouse(davenforToUpdate.getNameEnglishSpouse().trim());
				davenforToUpdate.setNameHebrewSpouse(davenforToUpdate.getNameHebrewSpouse().trim());
			}
		}

		if(!isAdmin) {
			davenforToUpdate.setSubmitterEmail(existingOrNewSubmitter(submitterEmail));
		}
		davenforToUpdate.setUpdatedAt(LocalDate.now());
		davenforToUpdate.setLastConfirmedAt(LocalDate.now());

		// Davenfor will expire in future according to it's category's settings.
		davenforToUpdate.setExpireAt(LocalDate.now().plusDays(davenforToUpdate.getCategory().getUpdateRate()));

		davenforRepository.save(davenforToUpdate);

	//	if (getMyGroupSettings(adminId).isNewNamePrompt()) {
			String subject = EmailScheme.getInformAdminOfUpdateSubject();
			String message = String.format(EmailScheme.getInformAdminOfUpdate(), davenforToUpdate.getSubmitterEmail(),
					davenforToUpdate.getNameEnglish(), davenforToUpdate.getNameHebrew(),
					davenforToUpdate.getCategory().getCname());
			emailSender.informAdmin(subject, message);
	//	}

		return davenforToUpdate;

	}

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

		if (!davenforToExtend.getSubmitterEmail().equalsIgnoreCase(submitterEmail)) {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it.");
		}

		// Extending the davenfor's expiration date according to the defined length in
		// its category.
		//LocalDate extendedDate = LocalDate.now().plusDays(categoryRepository.findByName(davenforToExtend.getCatType().toString()).getUpdateRate());
		//todo! - how to extend date correctly?  Gives error.
		LocalDate extendedDate = LocalDate.now().plusDays(30);
		davenforToExtend.setUpdatedAt(LocalDate.now());
		davenforToExtend.setLastConfirmedAt(LocalDate.now());
		davenforRepository.extendExpiryDate(davenforId, extendedDate, LocalDate.now());

		return true;
	}

	public List<Davenfor> deleteDavenfor(long davenforId, String submitterEmail)
			throws ObjectNotFoundException, PermissionException {
		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}
		Davenfor davenforToDelete = optionalDavenfor.get();
		String email = submitterEmail.trim();
		if (davenforToDelete.getSubmitterEmail().equalsIgnoreCase(email)) {
			davenforRepository.delete(davenforToDelete);
		} else {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it.");
		}
		return davenforRepository.findAllDavenforBySubmitterEmail(submitterEmail);
	}

	public List<Category> getAllCategories() throws ObjectNotFoundException {
		List<Category> categories = categoryRepository.findAllOrderById();
		if (categories.size()<1)
			throw new ObjectNotFoundException("System categories");
		return categories;
	}

	// Private helper method for Finding submitter according to email
	public String existingOrNewSubmitter(String submitterEmail) {
		Submitter validSubmitter = submitterRepository.findByEmail(submitterEmail);

		// If submitter has never submitted a name, need to create a new one in
		// database.
		if (validSubmitter == null) {
			validSubmitter = submitterRepository.save(new Submitter(submitterEmail));
		}
		return submitterEmail;
	}

	public Category getCategory(long id) throws ObjectNotFoundException {
		Optional<Category> optionalCategory = categoryRepository.findById(id);

		// We are not sure category is present. If not found, will throw an
		// exception.
		if (!optionalCategory.isPresent()) {
			throw new ObjectNotFoundException("Category with id " + id);
		}

		return optionalCategory.get();
	}

}
