package com.aliza.davening.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailSender;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;

import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.ObjectNotFoundException;
import exceptions.PermissionException;

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

	public Admin getMyGroupSettings(long adminId) throws ObjectNotFoundException {

		Optional<Admin> groupSettings = adminRepository.findById(adminId);
		if (groupSettings.isEmpty()) {
			throw new ObjectNotFoundException("Davening group with id " + adminId);

		}
		return groupSettings.get();

	}

	// According to email address submitter can see all names he submitted.
	public List<Davenfor> getAllSubmitterDavenfors(String email) {
		return davenforRepository.findAllDavenforBySubmitterEmail(email);
	}

	public Boolean emailConfirmation(Davenfor davenfor) throws EmailException, IOException {

		emailSender.sendConfirmationEmail(davenfor);

		return true;
	}

	public Davenfor addDavenfor(Davenfor davenfor, String submitterEmail)
			throws EmptyInformationException, ObjectNotFoundException, EmailException {

		// Trim all names nicely
		davenfor.setNameEnglish(davenfor.getNameEnglish().trim());
		davenfor.setNameHebrew(davenfor.getNameHebrew().trim());

		// If davenfor needs 2 names (e.g. banim), validate that second name is in
		// too, and if indeed exist - trim them.
		if ("banim".equals(davenfor.getCategory().getEnglish())) {
			if (davenfor.getNameEnglishSpouse().equals(null) || davenfor.getNameHebrewSpouse().equals(null)) {
				throw new EmptyInformationException("This category requires also a spouse name to be submitted. ");
			} else {
				davenfor.setNameEnglishSpouse(davenfor.getNameEnglishSpouse().trim());
				davenfor.setNameHebrewSpouse(davenfor.getNameHebrewSpouse().trim());
			}
		}

		davenfor.setSubmitter(existingOrNewSubmitter(submitterEmail));

		davenfor.setCreatedAt(LocalDate.now());
		davenfor.setLastConfirmedAt(LocalDate.now());

		// Davenfor will expire in future according to it's category's settings.
		davenfor.setExpireAt(LocalDate.now().plusDays(davenfor.getCategory().getUpdateRate()));

		davenforRepository.save(davenfor);

		// If admin's setting defines that admin should get an email upon new name being
		// added:
		if (getMyGroupSettings(SchemeValues.adminId).isNewNamePrompt()) {
			String subject = "A new name has been added to your davening list. ";
			String message = String.format(
					"The name: <br><b>%s <br> %s </b><br> has been added to the category: <br> <b>%s. </b><br> by <b>%s</b>"
							+ "<br><br> You might want to check that it was properly entered. ",
					davenfor.getNameEnglish(), davenfor.getNameHebrew(), davenfor.getCategory().getEnglish(),
					submitterEmail);
			emailSender.informAdmin(subject, message);
		}

		return davenfor;

	}

	public Davenfor updateDavenfor(Davenfor davenforToUpdate, String submitterEmail)
			throws EmptyInformationException, ObjectNotFoundException, EmailException, PermissionException {
		// TODO: see if can update through patch, or if worthwhile.

		if (davenforToUpdate == null) {
			throw new EmptyInformationException("No information submitted regarding the name information to update. ");
		}

		// Extracting id since it may be used more than once.
		long id = davenforToUpdate.getId();

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(id);
		if (optionalDavenfor.isEmpty()) {
			throw new ObjectNotFoundException("Name with id: " + id);
		}

		// Comparing email with davenfor-submitter from Database, since the davenfor
		// coming in may have empty email and lead to null pointer exception.
		if (!optionalDavenfor.get().getSubmitter().getEmail().equalsIgnoreCase(submitterEmail)) {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it.");
		}

		// Trim all names nicely
		davenforToUpdate.setNameEnglish(davenforToUpdate.getNameEnglish().trim());
		davenforToUpdate.setNameHebrew(davenforToUpdate.getNameHebrew().trim());

		// If davenfor needs 2 names (e.g. banim), validate that second name is in
		// too, and if indeed exist - trim them.
		if ("banim".equals(davenforToUpdate.getCategory().getEnglish())) {
			if (davenforToUpdate.getNameEnglishSpouse().equals(null)
					|| davenforToUpdate.getNameHebrewSpouse().equals(null)) {
				throw new EmptyInformationException("This category requires also a spouse name to be submitted. ");
			} else {
				davenforToUpdate.setNameEnglishSpouse(davenforToUpdate.getNameEnglishSpouse().trim());
				davenforToUpdate.setNameHebrewSpouse(davenforToUpdate.getNameHebrewSpouse().trim());
			}
		}

		davenforToUpdate.setSubmitter(existingOrNewSubmitter(submitterEmail));

		davenforToUpdate.setUpdatedAt(LocalDate.now());
		davenforToUpdate.setLastConfirmedAt(LocalDate.now());

		// Davenfor will expire in future according to it's category's settings.
		davenforToUpdate.setExpireAt(LocalDate.now().plusDays(davenforToUpdate.getCategory().getUpdateRate()));

		davenforRepository.save(davenforToUpdate);

		// If admin's setting defines that admin should get an email upon new name being
		// added:
		if (getMyGroupSettings(SchemeValues.adminId).isNewNamePrompt()) {
			String subject = "A name has been updated on your davening list. ";
			String message = String.format(
					"<b>%s</b> has just updated the name: <br><b>%s <br> %s </b><br>  in the category: <br> <b>%s. </b><br> "
							+ "<br><br> You might want to check that it was properly updated. ",
					submitterEmail, davenforToUpdate.getNameEnglish(), davenforToUpdate.getNameHebrew(),
					davenforToUpdate.getCategory().getEnglish());
			emailSender.informAdmin(subject, message);
		}

		return davenforToUpdate;

	}

	public Davenfor extendDavenfor(long davenforId, String email) throws ObjectNotFoundException, PermissionException {

		// TODO: would patch be appropriate here?

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (optionalDavenfor.isEmpty()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor davenforToExtend = optionalDavenfor.get();

		if (!davenforToExtend.getSubmitter().getEmail().equalsIgnoreCase(email)) {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it.");
		}

		// Extending the davenfor's expiration date according to the defined length in
		// its category.
		davenforToExtend.setExpireAt(LocalDate.now().plusDays(davenforToExtend.getCategory().getUpdateRate()));
		davenforToExtend.setUpdatedAt(LocalDate.now());
		davenforToExtend.setLastConfirmedAt(LocalDate.now());
		davenforRepository.save(davenforToExtend);

		return davenforToExtend;

	}

	public void deleteDavenfor(long davenforId, String submitterEmail)
			throws ObjectNotFoundException, PermissionException {
		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (optionalDavenfor.isEmpty()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}
		Davenfor davenforToDelete = optionalDavenfor.get();
		if (davenforToDelete.getSubmitter().getEmail().equalsIgnoreCase(submitterEmail)) {
			davenforRepository.delete(davenforToDelete);
		} else {
			throw new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it.");
		}
	}

	// Private helper method for Finding submitter according to email
	private Submitter existingOrNewSubmitter(String submitterEmail) {
		Submitter validSubmitter = submitterRepository.findByEmail(submitterEmail);

		// If submitter has never submitted a name, need to create a new one in
		// database.
		if (validSubmitter == null) {
			validSubmitter = submitterRepository.save(new Submitter(submitterEmail));
		}
		return validSubmitter;
	}

}
