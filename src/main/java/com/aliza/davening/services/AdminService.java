package com.aliza.davening.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.SchemeValues;
import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davener;
import com.aliza.davening.entities.Davenfor;
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
@EnableTransactionManagement
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
	public static Admin thisAdmin = new Admin(11, "davening.list@gmail.com", "adminPass66", false, 7);

	public Admin login(String email, String password) {
		Optional<Admin> optionalAdmin = adminRepository.getAdminByEmailAndPassword(email, password);
		if (!optionalAdmin.isPresent()) {
			return null;
		}
		thisAdmin = optionalAdmin.get();
		return thisAdmin;
	}

	/*
	 * This method is used both on initial start (when admin.id=0, will send a newly
	 * created Admin), and also to update admin with his existing id.
	 */
	public Admin setAdmin(Admin admin) throws DatabaseException {

		// ensuring no two admins have the same email
		if (isThisAdminEmailInUse(admin)) {
			throw new DatabaseException("This admin email address is already in use.");
		}
		adminRepository.save(admin);
		return admin;
	}

	public List<Davener> getAllDaveners() {
		return davenerRepository.findAll();
	}

	public String addDavener(Davener davener) throws NoRelatedEmailException {

		// saving davener's email, as it may be used multiple times in this method.
		String davenersEmail = davener.getEmail();

		// Lack of email needs to be detected before trying to save to DB, since other
		// actions are performed.
		if (davenersEmail == null) {
			throw new NoRelatedEmailException("Cannot enter this davener into the system.  No email associated. ");
		}

		// preparing return message for different cases.
		String returnMessage = String.format("We will now send weekly davening lists to %s. ", davenersEmail);

		/*
		 * If davener already exists on database (but was disactivated at a different
		 * time from receiving weekly emails), will change to active and save him.
		 * Otherwise, we will just add him to the database.
		 */
		if (davenerRepository.findByEmail(davenersEmail) != null) {
			davener.setActive(true);
			returnMessage = returnMessage.concat(" (By the way, this email existed on the database already.) ");
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

	public Davener updateDavener(Davener davener) throws ObjectNotFoundException, EmptyInformationException {

		if (davener == null) {
			throw new EmptyInformationException("Could not update.  No davener information sent. ");
		}

		// Checking that davener exists so that it won't create a new one through
		// save().
		Optional<Davener> optionalDavener = davenerRepository.findById(davener.getId());
		if (!optionalDavener.isPresent()) {
			throw new ObjectNotFoundException("Davener with id " + davener.getId());
		}
		// In case the external davenerId is different than the id sent with the davener
		// object
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

	
	@Transactional(rollbackFor = ReorderCategoriesException.class)
	public List<Category> changeCategoryOrder(List<Category> sortedCategories)
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
					"Categories reordered only partially. Could not continue with action. ");
		}

		return sortedCategories;
	}

	public Category addCategory(Category category) throws DatabaseException, EmptyInformationException {

		// Checking if added category is null
		if (category == null) {
			throw new EmptyInformationException("Could not add category.  No information was sent.");
		}

		// Removing any leading and trailing spaces from name.
		String englishCategoryName = category.getEnglish().trim();
		String hebrewCategoryName = category.getHebrew().trim();

		// Checking if category name is unique, English and Hebrew
		checkIfThisCategoryNameIsInUse(englishCategoryName, hebrewCategoryName, getAllCategories(),
				SchemeValues.NON_EXIST);

		// The trimmed name must be also saved in the entity to be persisted.
		category.setEnglish(englishCategoryName);
		category.setHebrew(hebrewCategoryName);

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

		// Trim incoming names before comparing them.
		String suggestedCategoryNameEnglish = categoryToUpdate.getEnglish().trim();
		String suggestedCategoryNameHebrew = categoryToUpdate.getHebrew().trim();

		/*
		 * checking if name is being updated, and if so that it is not already in use.
		 * No need to exclude comparing with name itself because sent only if not
		 * matching.
		 */
		boolean englishNameHasBeenChanged = !optionalCategory.get().getEnglish()
				.equalsIgnoreCase(suggestedCategoryNameEnglish);
		boolean hebrewNameHasBeenChanged = !optionalCategory.get().getHebrew()
				.equalsIgnoreCase(suggestedCategoryNameHebrew);
		if (englishNameHasBeenChanged || hebrewNameHasBeenChanged) {
			checkIfThisCategoryNameIsInUse(suggestedCategoryNameEnglish, suggestedCategoryNameHebrew,
					getAllCategories(), categoryId);

		}

		// Setting names in Category object in any case (even if name is the same,
		// spaces may have changed).
		categoryToUpdate.setEnglish(suggestedCategoryNameEnglish);
		categoryToUpdate.setHebrew(suggestedCategoryNameHebrew);

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

	public void disactivateDavener(String davenerEmail)
			throws EmailException, DatabaseException, ObjectNotFoundException, EmptyInformationException {

		Davener davenerToDisactivate = davenerRepository.findByEmail(davenerEmail);
		if (davenerToDisactivate.isActive() == false) {
			throw new DatabaseException(String.format(
					"The email %s has already been disactivated from receiving the davening lists. ", davenerEmail));
		}

		davenerRepository.disactivateDavener(davenerEmail);
		emailSender.notifyDisactivatedDavener(davenerEmail);

	}

	private boolean checkIfThisCategoryNameIsInUse(String english, String hebrew, List<Category> categories, long id)
			throws DatabaseException {

		// names should be compared only after trimming, as spaces do not change the
		// word inherently
		english = english.toLowerCase().trim();
		hebrew = hebrew.trim();

		for (Category c : categories) {
			if (c.getId() != id) { // do this check only if checked category is not the one in question

				if (english.equalsIgnoreCase(c.getEnglish()))
					throw new DatabaseException("The category name '" + english + "' is already in use.");
				if (hebrew.equalsIgnoreCase(c.getHebrew()))
					throw new DatabaseException("The category name '" + hebrew + "' is already in use.");

			}
		}

		return false;
	}

	// A local method to check if an admin email exists (other than this one, of
	// course).
	public boolean isThisAdminEmailInUse(Admin admin) {
		List<Admin> allAdmins = adminRepository.findAll();
		String thisAdminsEmail = admin.getEmail();
		for (Admin a : allAdmins) {
			if (a.getEmail().equalsIgnoreCase(thisAdminsEmail) && a.getId() != admin.getId()) {
				return true;
			}
		}
		return false;
	}

}
