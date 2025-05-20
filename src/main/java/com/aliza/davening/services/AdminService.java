package com.aliza.davening.services;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUAH;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.SchemeValues;
import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.entities.User;
import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.NoRelatedEmailException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.repositories.UserRepository;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.security.LoginRequest;
import com.aliza.davening.util_classes.AdminSettings;
import com.aliza.davening.util_classes.CategoryComparator;
import com.aliza.davening.util_classes.Weekly;

import io.jsonwebtoken.ExpiredJwtException;

@Service("adminService")
@EnableTransactionManagement
@Transactional
public class AdminService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	AdminRepository adminRepository;

	@Autowired
	ParashaRepository parashaRepository;

	@Autowired
	EmailSender emailSender;

	@Autowired
	Utilities utilities;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	EntityManager entityManager;

	@Autowired
	JwtUtils jwtUtils;

	public Admin thisAdmin = null;

	@Value("${admin.id}")
	long adminId;

	@Value("${admin.email}")
	String adminEmail;

	@PostConstruct
	public void initializeCategories() {
		Category.categories = categoryRepository.findAll();
		if (Category.categories.size() == 0) {
			categoryRepository.save(new Category(REFUA, false, 180, 1));
			categoryRepository.save(new Category(SHIDDUCHIM, true, 40, 2));
			categoryRepository.save(new Category(BANIM, false, 50, 3));
			categoryRepository.save(new Category(SOLDIERS, false, 30, 4));
			categoryRepository.save(new Category(YESHUAH, false, 180, 5));
		}

		Category.categories = Arrays.asList(new Category(REFUA, false, 180, 1), new Category(SHIDDUCHIM, true, 40, 2),
				new Category(BANIM, false, 50, 3), new Category(SOLDIERS, false, 30, 4),
				new Category(YESHUAH, false, 180, 5)); //todo* in future - why twice? make more efficient
	}

	/*
	 * This method is used on initial start (when admin.id=Non_Exist, will send a
	 * newly created Admin).
	 */

	// tested
	public boolean setAdmin(LoginRequest credentials) throws DatabaseException {

		/*
		 * ensuring no two admins have the same email. In this case sending to compare
		 * with Non_Exist since no need to compare with this id (not in the DB yet)
		 */
		if (isThisAdminEmailInUse(SchemeValues.NON_EXIST, credentials.getUsername())) {
			throw new DatabaseException("This admin email address is already in use.");
		}

		Admin admin = new Admin();

		admin.setId(SchemeValues.NON_EXIST);// Ensuring the DB will enter a new row.
		admin.setEmail(credentials.getUsername());
		admin.setPassword(bCryptPasswordEncoder.encode(credentials.getPassword()));
		admin.setWaitBeforeDeletion(SchemeValues.waitBeforeDeletion);
		admin.setNewNamePrompt(SchemeValues.adminNewNamePrompt);

		adminRepository.save(admin);
		return true;
	}

	// tested
	public Admin findAdminByEmail(String email) throws ObjectNotFoundException {
		Optional<Admin> optionalAdmin = adminRepository.getAdminByEmail(email);
		if (!optionalAdmin.isPresent()) {
			throw new ObjectNotFoundException("Admin with email " + email);
		}
		return optionalAdmin.get();
	}

	// tested
	public boolean updateAdmin(AdminSettings settings) throws ObjectNotFoundException {

		Optional<Admin> optionalAdmin = adminRepository.findByEmail(settings.getEmail());
		if (!optionalAdmin.isPresent()) {
			throw new ObjectNotFoundException("Admin with email " + settings.getEmail());
		}

		adminRepository.updateSettings(optionalAdmin.get().getId(), settings.getEmail(), settings.isNewNamePrompt(),
				settings.getWaitBeforeDeletion());
		entityManager.flush();
		entityManager.clear();
		return true;
	}

	// tested
	public boolean checkPassword(String password, String email) throws ObjectNotFoundException {
		Optional<Admin> optionalAdmin = adminRepository.getAdminByEmail(email);
		if (!optionalAdmin.isPresent()) {
			throw new ObjectNotFoundException("Admin with email " + email);
		}

		boolean answer = bCryptPasswordEncoder.matches(password, optionalAdmin.get().getPassword());
		if (!answer)
			System.out.println("Password is incorrect");
		return answer;
	}

	// tested
	public int getWaitBeforeDeletion(long id) {
		return adminRepository.getWaitBeforeDeletion(id);
	}

	// tested
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	// tested
	public List<User> addUser(User user) throws NoRelatedEmailException {

		String userEmail = user.getEmail();

		// Lack of email needs to be detected before trying to save to DB, since other
		// actions are performed.
		if (userEmail == null) {
			throw new NoRelatedEmailException("Cannot enter this user into the system.  No email associated. ");
		}

		/*
		 * If davener already exists on database (but was disactivated at a different
		 * time from receiving weekly emails), will change to active and save him under
		 * the same user
		 */
		Optional<User> existingUser = userRepository.findByEmail(userEmail);

		if (existingUser.isPresent()) {
			user = existingUser.get(); // giving davener the existing id
			user.setActive(true);
		} else { // new davener - save full incoming data
			userRepository.save(user);
		}

		return userRepository.findAll();
	}

	// tested
	public User getUser(long id) throws ObjectNotFoundException {
		Optional<User> optionalUser = userRepository.findById(id);

		if (!optionalUser.isPresent()) {
			throw new ObjectNotFoundException("User with id " + id);
		}

		return optionalUser.get();
	}

	// tested
	public List<User> updateUser(User user) throws ObjectNotFoundException {

		// Checking that davener exists so that it won't create a new one through
		// save().
		Optional<User> optionalUser = userRepository.findById(user.getId());
		if (!optionalUser.isPresent()) {
			throw new ObjectNotFoundException("Davener with id " + user.getId());
		}
		// In case the external davenerId is different than the id sent with the davener
		// object
		userRepository.save(user);
		return userRepository.findAll();
	}

	// tested
	public List<User> deleteUser(long id) throws ObjectNotFoundException {
		Optional<User> optionalUser = userRepository.findById(id);
		if (!optionalUser.isPresent()) {
			throw new ObjectNotFoundException("User with id: " + id);
		}
		userRepository.delete(optionalUser.get());
		return userRepository.findAll();
	}

	// tested
	public List<User> getAllSubmitters() {
		return userRepository.findAll();
	}

	// tested
	public List<Davenfor> getAllDavenfors() {
		List<Davenfor> allDavenfors = davenforRepository.findAll();
		Collections.sort(allDavenfors, new CategoryComparator());
		return allDavenfors;
	}

	// tested
	public List<Davenfor> deleteDavenfor(long id) throws ObjectNotFoundException {
		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(id);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + id);
		}
		davenforRepository.delete(optionalDavenfor.get());
		return davenforRepository.findAll();
	}

	// TODO*: enable in future
	// A helper method set up primarily for changeCategoryOrder()
//	private Category getCategory(long id) throws ObjectNotFoundException {
//		Optional<Category> optionalCategory = categoryRepository.findById(id);
//		if (!optionalCategory.isPresent()) {
//			throw new ObjectNotFoundException("Category of id " + id);
//		}
//		return optionalCategory.get();
//	}

	// TODO*: fix. when works, test
//	@Transactional(rollbackFor = ReorderCategoriesException.class)
//	public List<Category> changeCategoryOrder(List<Category> sortedCategories)
//			throws ReorderCategoriesException, ObjectNotFoundException {
//
//		/*
//		 * as we update the categories, we will cross them off, and check at the end if
//		 * all have been crossed off.
//		 */
//
//		List<Category> crossOffList = categoryRepository.findAll();
//
//		for (Category newCategory : sortedCategories) {
//
//			// checking if new category order is valid.
//			if (newCategory.getCatOrder() < 0) {
//				throw new ReorderCategoriesException("Category order must be a positive number.");
//			}
//
//			Category updatedCategory = getCategory(newCategory.getId());
//			categoryRepository.updateCategoryOrder(newCategory.getCatOrder(), newCategory.getId());
//
//			crossOffList.remove(updatedCategory);
//		}
//
//		// checking in cross-off list if any categories have not been included in the
//		// sort.
//		if (!crossOffList.isEmpty()) {
//			throw new ReorderCategoriesException(
//					"Categories reordered only partially. Could not continue with action. ");
//		}
//
//		return sortedCategories;
//	}

	// TODO*: when ready, implement and test
	/*
	 * public Category addCategory(Category category) throws DatabaseException,
	 * EmptyInformationException {
	 * 
	 * // Checking if added category is null if (category == null) { throw new
	 * EmptyInformationException("Could not add category.  No information was sent."
	 * ); }
	 * 
	 * // Removing any leading and trailing spaces from name. String
	 * englishCategoryName = category.getEnglish().trim(); String hebrewCategoryName
	 * = category.getHebrew().trim();
	 * 
	 * // Checking if category name is unique, English and Hebrew
	 * checkIfThisCategoryNameIsInUse(englishCategoryName, hebrewCategoryName,
	 * getAllCategories(), SchemeValues.NON_EXIST);
	 * 
	 * // The trimmed name must be also saved in the entity to be persisted.
	 * category.setEnglish(englishCategoryName);
	 * category.setHebrew(hebrewCategoryName);
	 * 
	 * categoryRepository.save(category);
	 * 
	 * return category; }
	 * 
	 * public Category updateCategory(long categoryId, Category categoryToUpdate)
	 * throws ObjectNotFoundException, EmptyInformationException, DatabaseException
	 * {
	 * 
	 * // Checking that category sent in is not null if (categoryToUpdate == null) {
	 * throw new
	 * EmptyInformationException("Could not update category.  No information was sent. "
	 * ); }
	 * 
	 * // Checking that this is an existing category so that won't create a new one
	 * // through save(). Optional<Category> optionalCategory =
	 * categoryRepository.findById(categoryId); if (!optionalCategory.isPresent()) {
	 * throw new ObjectNotFoundException("Category with id " + categoryId); }
	 * 
	 * /* Once we verified that the category to update is an existing valid one
	 * (judging also by the id), we can use the verified ID in case the external
	 * categoryId is different than the id sent with the category object
	 * 
	 * categoryToUpdate.setId(categoryId);
	 * 
	 * // Trim incoming names before comparing them. String
	 * suggestedCategoryNameEnglish = categoryToUpdate.getEnglish().trim(); String
	 * suggestedCategoryNameHebrew = categoryToUpdate.getHebrew().trim();
	 * 
	 * /* checking if name is being updated, and if so that it is not already in
	 * use. No need to exclude comparing with name itself because sent only if not
	 * matching.
	 * 
	 * boolean englishNameHasBeenChanged = !optionalCategory.get().getEnglish()
	 * .equalsIgnoreCase(suggestedCategoryNameEnglish); boolean
	 * hebrewNameHasBeenChanged = !optionalCategory.get().getHebrew()
	 * .equalsIgnoreCase(suggestedCategoryNameHebrew); if (englishNameHasBeenChanged
	 * || hebrewNameHasBeenChanged) {
	 * checkIfThisCategoryNameIsInUse(suggestedCategoryNameEnglish,
	 * suggestedCategoryNameHebrew, getAllCategories(), categoryId);
	 * 
	 * }
	 * 
	 * // Setting names in Category object in any case (even if name is the same, //
	 * spaces may have changed).
	 * categoryToUpdate.setEnglish(suggestedCategoryNameEnglish);
	 * categoryToUpdate.setHebrew(suggestedCategoryNameHebrew);
	 * 
	 * categoryRepository.save(categoryToUpdate); return categoryToUpdate; }
	 * 
	 * public void deleteCategory(long id) throws ObjectNotFoundException { try {
	 * categoryRepository.deleteById(id); // If not found, will throw an unchecked
	 * exception which we must catch. } catch (EmptyResultDataAccessException e) {
	 * throw new ObjectNotFoundException("Category with id " + id); } }
	 */

	// tested
	public List<User> disactivateUser(String userEmail) throws EmptyInformationException {
		List<User> userList = null;
		try {
			Optional<User> userToDisactivate = userRepository.findByEmail(userEmail);
			if (userToDisactivate.isEmpty()) { // TODO* - add test that user not found (really can't get empty
												// email because "" doesn't go to link)
				System.out.println(String.format(
						"The email %s cannot be disactivated because it is not found.  Please check the email address. ",
						userEmail));
				return userRepository.findAll();
			}
			if (!userToDisactivate.get().isActive()) { // Just to log/notify, and continue business as usual,
														// returning most recent users list.
				System.out.println(String.format(
						"The email %s has already been disactivated from receiving the davening lists. ", userEmail));
			}

			else {
				userRepository.disactivateUser(userEmail);
				entityManager.flush();
				entityManager.clear();
				emailSender.notifyDisactivatedUser(userEmail);
			}
		} finally { // in case there were previous errors (such as in emailSender), return
					// userList anyway.
			userList = userRepository.findAll();
		}
		return userList;
	}

	// tested
	public List<User> activateUser(String userEmail) throws EmptyInformationException {

		List<User> userList = null;

		try {
			Optional<User> userToActivate = userRepository.findByEmail(userEmail);
			if (userToActivate.isEmpty()) { // TODO* - add test that user not found (really can't get empty
				// email because "" doesn't go to link)
				System.out.println(String.format(
						"The email %s cannot be activated because it is not found.  Please check the email address. ",
						userEmail));
				return userRepository.findAll();
			}
			if (userToActivate.get().isActive() == true) { // Just to log/notify, and continue business as usual,
															// returning
				// most recent daveners list.
				System.out.println(String.format("The email %s is already receiving the davening lists. ", userEmail));
			}

			else {
				userRepository.activateUser(userEmail);
				entityManager.flush();
				entityManager.clear();
				emailSender.notifyActivatedUser(userEmail);
			}
		} finally {// in case there were previous errors (such as in emailSender), return
			// userList anyway.
			userList = userRepository.findAll();
		}
		return userList;
	}

	// tested
	public void updateCurrentCategory() {
		// Checking which is the next category in line. Changing it's isCurrent to true,
		// while the previous one to false.
		Category currentCategory = categoryRepository.getCurrent().get();
		Category nextCategory = utilities.getNextCategory(currentCategory);
		categoryRepository.updateCategoryCurrent(false, currentCategory.getId());
		categoryRepository.updateCategoryCurrent(true, nextCategory.getId());
	}

	// TODO*: test
	public void updateParasha() {
		Optional<Parasha> currentParashaOptional = parashaRepository.findCurrent();
		if (currentParashaOptional.isEmpty())
			System.out.println("No current parasha was found");
		else {
			Parasha currentParasha = currentParashaOptional.get();
			Parasha nextParasha = utilities.getNextParasha(currentParasha);
			parashaRepository.updateParashaCurrent(false, currentParasha.getId());
			parashaRepository.updateParashaCurrent(true, nextParasha.getId());
		}
	}

	// tested
	public List<Category> getAllCategories() {
		return categoryRepository.findAllOrderById();
	}

	// tested indirectly. A local method to check if an admin email exists (other
	// than this one, of
	// course).
	private boolean isThisAdminEmailInUse(long id, String email) {
		List<Admin> allAdmins = adminRepository.findAll();
		for (Admin a : allAdmins) {
			if (a.getEmail().equalsIgnoreCase(email) && a.getId() != id) {
				return true;
			}
		}
		return false;
	}

	// tested
	public List<Parasha> getAllParashot() {
		return this.parashaRepository.findAll();
	}

	// tested
	public Parasha findCurrentParasha() {
		return this.parashaRepository.findCurrent().get();
	}

	// tested
	public Category findCurrentCategory() {
		return this.categoryRepository.getCurrent().get();
	}

	// todo* in future: test now that changed
	public String previewWeekly(Weekly info) throws ObjectNotFoundException, EmptyInformationException {
		//TODO* in future - test if came in null
		Category category;
		String parashaNameFull;

		if (info == null) // came from direct, or other issue
		{
			try {
				category = inferCategory();
				parashaNameFull = inferParashaName(true);
			} catch (ObjectNotFoundException ex) {
				throw ex;
			}
		}

		else // info came in
		{
			category = Category.getCategory(info.category);
			if (category == null) {
				throw new ObjectNotFoundException("category named " + info.category);
			}
			parashaNameFull = info.parashaNameFull;
		}

		return utilities.createWeeklyHtml(category, parashaNameFull, true);
	}

	// tested
	public AdminSettings getAdminSettings(String email) throws ObjectNotFoundException {
		Admin admin = findAdminByEmail(email);
		return new AdminSettings(admin.getEmail(), admin.isNewNamePrompt(), admin.getWaitBeforeDeletion());
	}

	// to test
	// verifying that email to be saved in frontend is good
	public boolean checkTokenForDirect(String token, String email) {
		try {
			String extractedEmail = jwtUtils.extractEmailFromToken(token);
			System.out.println("Extracted email is: " + extractedEmail);
			if (extractedEmail == null)
				return false;
			if (!extractedEmail.equalsIgnoreCase(adminEmail) || !extractedEmail.equalsIgnoreCase(email)) {
				System.out.println("The email doesn't match the registered admin email");
				return false;
			}
		} catch (ExpiredJwtException e) {
			throw e;
		}

		return true;
	}

	public Category inferCategory() throws ObjectNotFoundException {
		Category category = categoryRepository.getCurrent()
				.orElseThrow(() -> new ObjectNotFoundException("current category"));
		return category;
	}

	public String inferParashaName(boolean full) throws ObjectNotFoundException {
		Parasha parasha = parashaRepository.findCurrent()
				.orElseThrow(() -> new ObjectNotFoundException("current Parasha"));
		if (full)
			return parasha.getEnglishName() + " - " + parasha.getHebrewName();
		else
			return parasha.getEnglishName();
	}

//	private boolean checkIfThisCategoryNameIsInUse(String english, String hebrew, List<Category> categories, long id)
//	throws DatabaseException {
//
//// names should be compared only after trimming, as spaces do not change the
//// word inherently
//english = english.toLowerCase().trim();
//hebrew = hebrew.trim();
//
//for (Category c : categories) {
//	if (c.getId() != id) { // do this check only if checked category is not the one in question
//
//		if (english.equalsIgnoreCase(c.getEnglish()))
//			throw new DatabaseException("The category name '" + english + "' is already in use.");
//		if (hebrew.equalsIgnoreCase(c.getHebrew()))
//			throw new DatabaseException("The category name '" + hebrew + "' is already in use.");
//
//	}
//}
//
//return false;
//}

}
