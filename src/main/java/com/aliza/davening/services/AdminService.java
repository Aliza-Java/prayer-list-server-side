package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailSender;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davener;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;

import exceptions.DatabaseException;
import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.NoRelatedEmailException;
import exceptions.ObjectNotFoundException;
import exceptions.ReorderCategoriesException;

@Service("adminService")
public class AdminService {

	@Autowired
	DavenerRepository davenerRepository;

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

	// TODO: change to null when have login system in place, so that by default it's
	// null and changes only if properly logged in.
	Admin thisAdmin = new Admin(5, "davening.list@gmail.com", "admin1234", false, 7);

	public Admin login(String email, String password) {
		Optional<Admin> optionalAdmin = adminRepository.getAdminByEmailAndPassword(email, password);
		if (!optionalAdmin.isPresent()) {
			return null;
		}
		thisAdmin = optionalAdmin.get();
		return thisAdmin;
	}

	// This method is used both on initial start (and id=0, will send a newly
	// created Admin),
	// and also to update admin, where id will be current session.adminId.
	public Admin setAdmin(Admin admin, long id) {
		admin.setId(id);
		adminRepository.save(admin);
		return admin;
	}

	public List<Davener> getAllDaveners() {
		return davenerRepository.findAll();
	}

	public String addDavener(Davener davener) throws NoRelatedEmailException {

		// saving davener's email, as it may be used multiple times in this method.
		String davenersEmail = davener.getEmail();

		if (davenersEmail == null) {
			throw new NoRelatedEmailException("Cannot enter this davener into the system.  No email associated. ");
		}

		// preparing return message for different cases.
		String returnMessage = String.format("We will now send weekly davening lists to %s. ", davenersEmail);

		/*
		 * If davener already exists on database (but was disactivated from receiving
		 * weekly emails), will change to active and save him. Otherwise, we will just
		 * add him to the database.
		 */
		if (davenerRepository.findByEmail(davenersEmail) != null) {
			davener.setActive(true);
			returnMessage = returnMessage.concat(" (By the way, this email existed on database already.) ");
		}
		davenerRepository.save(davener);
		return returnMessage;
	}

	public Davener getDavener(long id) throws ObjectNotFoundException {
		Optional<Davener> optionalDavener = davenerRepository.findById(id);

		// We are not sure he will be found by id. If not found, will throw an
		// exception.
		if (!optionalDavener.isPresent()) {
			throw new ObjectNotFoundException("Davener with id " + id);
		}

		return optionalDavener.get();
	}

	public Davener updateDavener(long davenerId, Davener davener)
			throws ObjectNotFoundException, EmptyInformationException {

		if (davener == null) {
			throw new EmptyInformationException("Could not update.  No davener information sent. ");
		}

		// Checking that davener exists so that it won't create a new one through
		// save().
		Optional<Davener> optionalDavener = davenerRepository.findById(davenerId);
		if (!optionalDavener.isPresent()) {
			throw new ObjectNotFoundException("Davener with id " + davenerId);
		}
		// In case the external davenerId is different than the id sent with the davener
		// object
		davener.setId(davenerId);
		davenerRepository.save(davener);
		return davener;
	}

	public void deleteDavener(long id) throws ObjectNotFoundException {
		try {
			davenerRepository.deleteById(id);
			// If not found, will throw an unchecked exception which we must catch.
		} catch (EmptyResultDataAccessException e) {
			throw new ObjectNotFoundException("Davener with id " + id);
		}
	}

	public List<Submitter> getAllSubmitters() {
		return submitterRepository.findAll();
	}

	public List<Davenfor> getAllDavenfors() {
		return davenforRepository.findAll();
	}

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	// A helper method set up primarily for changeCategoryOrder()
	private Category getCategory(long id) throws ObjectNotFoundException {
		Optional<Category> optionalCategory = categoryRepository.findById(id);
		if (!optionalCategory.isPresent()) {
			throw new ObjectNotFoundException("Category of id " + id);
		}
		return optionalCategory.get();
	}

	/*
	 * This entire action must be done completely. If something doesn't work out, it
	 * must roll back.
	 * 
	 * Returning void instead of findAll since the @Transactional causes findAll to
	 * be of previous version, before reordering. If necessary, retrieve all after
	 * entire action.
	 */
	@Transactional
	public void changeCategoryOrder(List<Category> sortedCategories)
			throws ReorderCategoriesException, ObjectNotFoundException {

		/*
		 * as we update the categories, we will cross them off, and check at the end if
		 * all have been crossed off.
		 */
		List<Category> crossOffList = categoryRepository.findAll();

		for (Category newCategory : sortedCategories) {

			// checking if new category order is valid.
			if (newCategory.getCatOrder() < 0) {
				throw new ReorderCategoriesException("Category order must be a positive number.");
			}

			Category updatedCategory = getCategory(newCategory.getId());
			categoryRepository.updateCategoryOrder(newCategory.getCatOrder(), newCategory.getId());
			crossOffList.remove(updatedCategory);
		}

		// checking in cross-off list if any categories have not been included in the
		// sort.
		if (!crossOffList.isEmpty()) {
			throw new ReorderCategoriesException(
					"Categories reordered only partially.  Could not continue with action. ");
		}
	}

	public Category addCategory(Category category) throws DatabaseException, EmptyInformationException {

		// Checking if added category is null
		if (category == null) {
			throw new EmptyInformationException("Could not add category.  No information was sent.");
		}

		// Removing any leading and trailing spaces from name.
		String categoryName = category.getEnglish().trim();

		// Checking if category name is unique.
		if (isThisCategoryNameInUse(categoryName, getAllCategories())) {
			throw new DatabaseException("The category name '" + categoryName.toLowerCase() + "' is already in use.");
		}

		// The trimmed name must be also saved in the entity to be persisted.
		category.setEnglish(categoryName);

		categoryRepository.save(category);
		return category;
	}

	public Category updateCategory(long categoryId, Category categoryToUpdate)
			throws ObjectNotFoundException, EmptyInformationException, DatabaseException {

		// Checking that category sent in is not null
		if (categoryToUpdate == null) {
			throw new EmptyInformationException("Could not update category.  No information was sent. ");
		}

		// Checking that this is an existing category so that won't create a new one
		// through save().
		Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
		if (!optionalCategory.isPresent()) {
			throw new ObjectNotFoundException("Category with id " + categoryId);
		}

		/*
		 * Once we verified that the category to update is an existing valid one
		 * (judging also by the id), we can use the verified ID in case the external
		 * categoryId is different than the id sent with the category object
		 */
		categoryToUpdate.setId(categoryId);

		/*
		 * checking if name is being updated, and if so that it is not already in use No
		 * need to exclude comparing with name itself because sent only if not matching.
		 */

		String suggestedCategoryName = categoryToUpdate.getEnglish().trim();
		if (!optionalCategory.get().getEnglish().equalsIgnoreCase(suggestedCategoryName)) {
			if (isThisCategoryNameInUse(suggestedCategoryName, getAllCategories())) {
				throw new DatabaseException(
						"The category name '" + suggestedCategoryName.toLowerCase() + "' is already in use.");
			}
		}

		// In case originally name had spaces and was then trimmed.
		categoryToUpdate.setEnglish(suggestedCategoryName);

		categoryRepository.save(categoryToUpdate);
		return categoryToUpdate;
	}

	public void deleteCategory(long id) throws ObjectNotFoundException {
		try {
			categoryRepository.deleteById(id);
			// If not found, will throw an unchecked exception which we must catch.
		} catch (EmptyResultDataAccessException e) {
			throw new ObjectNotFoundException("Category with id " + id);
		}
	}

	public void sendOutWeekly(Parasha parasha)
			throws EmptyInformationException, IOException, MessagingException, EmailException {
		if (parasha == null) {
			throw new EmptyInformationException("No Parasha name submitted. ");
		}

		emailSender.sendWeeklyEmailToAll(davenerRepository.getAllDavenersEmails(), categoryRepository.getCurrent(),
				parasha, buildWeeklyEmail(parasha));
	}

	public void sendOutUrgent(Davenfor davenfor, String davenforNote)
			throws EmptyInformationException, MessagingException, EmailException {
		if (davenfor == null) {
			throw new EmptyInformationException("The name you submitted for davening is incomplete.  ");
		}
		emailSender.sendUrgentEmail(davenerRepository.getAllDavenersEmails(), davenfor, davenforNote);

	}

	public File buildWeeklyEmail(Parasha parasha) throws IOException {

		Category currentCategory = categoryRepository.getCurrent();

		// The lines making up the file
		List<String> formattedLines = new ArrayList<String>();

		// Adding headlines
		formattedLines.add(String.format("%s %s", SchemeValues.inMemoryEnglish, SchemeValues.inMemoryHebrew));
		formattedLines.add(String.format("%s %s", parasha.getEnglishName(), parasha.getHebrewName()));
		formattedLines.add(String.format("%s %s", currentCategory.getEnglish(), currentCategory.getHebrew()));

		// Adding all names to the main part of the file
		List<Davenfor> categoryDavenfors = davenforRepository.findAllDavenforByCategory(currentCategory);

		for (Davenfor d : categoryDavenfors) {
			formattedLines.add(String.format("%s %s", d.getNameEnglish(), d.getNameHebrew()));
		}

		// Writing final closing lines in file
		Category nextCategory = getNextCategory(currentCategory);

		formattedLines
				.add(String.format("Next week %s - שבוע הבא %s", nextCategory.getEnglish(), nextCategory.getHebrew()));

		formattedLines.add(String.format("Please email %s with name and good news!", thisAdmin.getEmail()));

		// writing the lines to the file
		Path file = Paths.get("builtFiles/" + parasha.getFileName() + "_" + LocalDate.now().toString() + ".txt");
		Files.write(file, formattedLines, StandardCharsets.UTF_8);

		return file.toFile();

	}

	public void disactivateDavener(Davener davener) {
		davenerRepository.disactivateDavener(davener.getId());

	}

	public Category getNextCategory(Category current) {

		List<Category> allCategories = categoryRepository.findAll();

		int max = Integer.MAX_VALUE;
		int currentPosition = current.getCatOrder();
		Category nextCategory = null;

		/*
		 * Search for the category containing the very next cat_order - must be bigger
		 * than current position but lower than all other 'next' categories
		 */
		for (Category c : allCategories) {
			if (c.getCatOrder() > currentPosition && c.getCatOrder() < max) {
				nextCategory = c;
				max = c.getCatOrder();
			}
		}

		/*
		 * If didn't find the next in line, because current cat_order is the highest -
		 * restart rotation and search for lowest value.
		 */
		if (nextCategory == null) {
			int min = Integer.MAX_VALUE;
			for (Category c : allCategories) {
				if (c.getCatOrder() < min) {
					nextCategory = c;
					min = c.getCatOrder();
				}
			}
		}
		return nextCategory;
	}

	private boolean isThisCategoryNameInUse(String name, List<Category> categories) {
		for (Category c : categories) {
			if (name.equalsIgnoreCase(c.getEnglish())) {
				return true;
			}
		}
		return false;
	}

}
