package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUA_AND_PARNASSA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

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
import com.aliza.davening.security.LoginRequest;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.util_classes.AdminSettings;
import com.aliza.davening.util_classes.Weekly;

@ExtendWith(SpringExtension.class)
@Transactional // to allow for flush() and clear()
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceAdminTests {

	@MockBean
	private AuthenticationManager authenticationManager;

	@Autowired
	private AdminService adminService;

	@MockBean
	private DavenforRepository davenforRep;

	@MockBean
	private CategoryRepository categoryRep;

	@MockBean
	private AdminRepository adminRep;

	@MockBean
	private ParashaRepository parashaRep;

	@MockBean
	private UserRepository userRep;

	@MockBean
	private EmailSender emailSender;

	static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	static String adminEmail = "admin.email@gmail.com";
	static String adminRawPass = "pass1";
	static String adminPass = encoder.encode(adminRawPass);

	public static Category catRefua = new Category(REFUA, true, 180, 1);
	public static Category catShidduchim = new Category(SHIDDUCHIM, false, 40, 2);
	public static Category catBanim = new Category(BANIM, false, 50, 3);
	public static Category catSoldiers = new Category(SOLDIERS, false, 180, 4);
	public static Category catYeshua = new Category(YESHUA_AND_PARNASSA, false, 180, 5);
	public static List<Category> categories = Arrays.asList(catRefua, catShidduchim, catBanim, catSoldiers, catYeshua);

	public static Admin admin1 = new Admin(1, adminEmail, adminPass, false, 7);
	public static Admin admin2 = new Admin(2, "admin2@gmail.com", null, false, 7);
	public static Admin admin3 = new Admin(3, "admin3@gmail.com", null, false, 7);

	public static User user1 = new User(1, null, "user1@gmail.com", "Israel", null, null, false, "");
	public static User user2 = new User(2, null, "user2@gmail.com", "Israel", null, null, false, "");
	public static User user3 = new User(3, null, "user3@gmail.com", "Israel", null, null, true, "");
	public static List<User> users = Arrays.asList(user1, user2, user3);

	public static Parasha parasha1 = new Parasha(1, "Bereshit", "בראשית", true);
	public static Parasha parasha2 = new Parasha(2, "Noach", "נח", false);
	public static Parasha parasha3 = new Parasha(3, "Lech Lecha", "לך-לך", false);
	public static List<Parasha> parashot = Arrays.asList(parasha1, parasha2, parasha3);

	public static Davenfor dfRefua = new Davenfor(1L, "user1@gmail.com", "Refua", "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshua1 = new Davenfor(4, "user1@gmail.com", "Yeshua_and_Parnassa", "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "user2@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshua2 = new Davenfor(4, "user2@gmail.com", "Yeshua_and_Parnassa", "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshua1, dfBanim, dfYeshua2);

	private final static String UNEXPECTED_E = "   ************* Attention: @Admin service test unexpected Exception: ";

	@BeforeAll
	private void baseTest() {

		// TODO*:
		// in service tests - fix base test, make generals.commit all. (maybe later)

		// when(submitterRep.findByEmail(submitterEmail)).thenReturn(new
		// Submitter(submitterEmail));

		// TODO*: create integration tests for whole process including emailing

		when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(Optional.of(catShidduchim));
		when(categoryRep.findByCname(BANIM)).thenReturn(Optional.of(catBanim));
		when(categoryRep.findByCname(REFUA)).thenReturn(Optional.of(catRefua));
		when(categoryRep.findByCname(YESHUA_AND_PARNASSA)).thenReturn(Optional.of(catYeshua));
		when(categoryRep.findByCname(SOLDIERS)).thenReturn(Optional.of(catSoldiers));
	}

	@Test
	@Order(1)
	public void setAdminTest() {
		List<Admin> admins = Arrays.asList(admin1, admin2, admin3);

		when(adminRep.findAll()).thenReturn(admins);
		Exception exception = assertThrows(DatabaseException.class, () -> {
			adminService.setAdmin(new LoginRequest("admin2@gmail.com", "pass2"));
		});
		assertTrue(exception.getMessage().contains("in use"));

		try {
			assertTrue(adminService.setAdmin(new LoginRequest("admin4@gmail.com", "pass2")));
		} catch (DatabaseException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(adminRep, times(1)).save(any());
		verify(adminRep, times(2)).findAll();
	}

	@Test
	@Order(2)
	public void findAdminByEmailTest() {
		when(adminRep.getAdminByEmail("wrong.email@gmail.com")).thenReturn(Optional.empty());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.findAdminByEmail("wrong.email@gmail.com");
		});
		assertTrue(exception.getMessage().contains("with email"));

		when(adminRep.getAdminByEmail(adminEmail)).thenReturn(Optional.of(admin1));
		try {
			assertEquals(adminEmail, adminService.findAdminByEmail(adminEmail).getEmail());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(adminRep, times(2)).getAdminByEmail(any());
	}

	@Test
	@Order(3)
	public void updateAdminTest() {
		// id not found throws exception
		when(adminRep.findByEmail("newEmail@gmail.com")).thenReturn(Optional.empty());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.updateAdmin(new AdminSettings("newEmail@gmail.com", true, 10));
		});
		assertTrue(exception.getMessage().contains("Admin with email"));

		when(adminRep.findByEmail("updatedAdmin@gmail.com"))
				.thenReturn(Optional.of(new Admin(3, "updatedAdmin@gmail.com", "pass", true, 3)));
		try {
			assertTrue(adminService.updateAdmin(new AdminSettings("updatedAdmin@gmail.com", false, 8)));
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(adminRep, times(2)).findByEmail(any());
	}

	@Test
	@Order(4)
	public void checkPasswordTest() {
		when(adminRep.getAdminByEmail("wrong.email@gmail.com")).thenReturn(Optional.empty());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.checkPassword("pass", "wrong.email@gmail.com");
		});
		assertTrue(exception.getMessage().contains("email"));

		when(adminRep.getAdminByEmail(adminEmail)).thenReturn(Optional.of(admin1));
		try {
			assertTrue(adminService.checkPassword(adminRawPass, adminEmail));
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(adminRep, times(2)).getAdminByEmail(any());

	}

	@Test
	@Order(5)
	public void getWaitBeforeDeletionTest() {
		when(adminRep.getWaitBeforeDeletion(anyLong())).thenReturn(6);
		assertEquals(6, adminService.getWaitBeforeDeletion(533));

		verify(adminRep, times(1)).getWaitBeforeDeletion(anyLong());
	}

	@Test
	@Order(6)
	public void getAllUsersTest() {
		when(userRep.findAll()).thenReturn(users);
		assertEquals(3, adminService.getAllUsers().size());

		verify(userRep, times(1)).findAll();
	}

	@Test
	@Order(7)
	public void addUserTest() {
		user1.setEmail(null);
		Exception exception = assertThrows(NoRelatedEmailException.class, () -> {
			adminService.addUser(user1);
		});
		assertTrue(exception.getMessage().contains("No email"));

		when(userRep.findByEmail("user2@gmail.com")).thenReturn(Optional.of(user2));
		when(userRep.findAll()).thenReturn(users);
		user2.setActive(false);
		try {
			assertEquals(3, adminService.addUser(user2).size()); // should return 3 users
			assertTrue(user2.isActive());
		} catch (NoRelatedEmailException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		when(userRep.findByEmail("newUser@gmail.com")).thenReturn(null);
		user3.setActive(false);
		try {
			assertEquals(3, adminService.addUser(user3).size()); // should return 3 users
			assertFalse(user3.isActive());
		} catch (NoRelatedEmailException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(userRep, times(2)).findByEmail(any());
		verify(userRep, times(2)).findAll();

		// returning to original values
		user2.setActive(false);
		user3.setActive(true);
	}

	@Test
	@Order(8)
	public void getUserTest() {
		when(userRep.findById(4L)).thenReturn(Optional.empty());
		when(userRep.findById(3L)).thenReturn(Optional.of(user3));

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.getUser(4L);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			assertEquals(user3, adminService.getUser(3L));
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(userRep, times(2)).findById(anyLong());
	}

	@Test
	@Order(9)
	public void updateUserTest() {
		when(userRep.findById(1L)).thenReturn(Optional.empty());
		when(userRep.findById(3L)).thenReturn(Optional.of(user3));
		when(userRep.findAll()).thenReturn(users);

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.updateUser(user1);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			assertEquals(3, adminService.updateUser(user3).size());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(userRep, times(2)).findById(anyLong());
		verify(userRep, times(1)).save(any());
		verify(userRep, times(1)).findAll();
	}

	@Test
	@Order(10)
	public void deleteUserTest() {
		when(userRep.findAll()).thenReturn(Arrays.asList(user1, user2));
		when(userRep.findById(3L)).thenReturn(Optional.of(user3));

		when(userRep.findById(4L)).thenReturn(Optional.empty());

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.deleteUser(4L);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			List<User> usersLeft = adminService.deleteUser(3L);
			assertEquals(2, usersLeft.size());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(userRep, times(2)).findById(anyLong());
		verify(userRep, times(1)).findAll();
	}

	@Test
	@Order(12)
	public void getAllDavenforsTest() {
		when(davenforRep.findAll()).thenReturn(davenfors);
		assertEquals(4, adminService.getAllDavenfors().size());

		verify(davenforRep, times(1)).findAll();
	}

	@Test
	@Order(13)
	public void deleteDavenforTest() {
		when(davenforRep.findById(4L)).thenReturn(Optional.empty());

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.deleteDavenfor(4L);
		});
		assertTrue(exception.getMessage().contains("id"));

		when(davenforRep.findById(3L)).thenReturn(Optional.of(dfYeshua1));
		when(davenforRep.findAll()).thenReturn(davenfors);
		try {
			assertEquals(4, adminService.deleteDavenfor(3L).size());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(davenforRep, times(2)).findById(anyLong());
		verify(davenforRep, times(1)).findAll();
	}

	@Test
	@Order(14)
	public void deactivateUserTest() {
		when(userRep.findByEmail(user2.getEmail())).thenReturn(Optional.of(user2));
		when(userRep.findByEmail(user3.getEmail())).thenReturn(Optional.of(user3));
		when(userRep.findAll()).thenReturn(users);

		assertFalse(user2.isActive());
		assertTrue(user3.isActive());

		try {
			adminService.deactivateUser("user2@gmail.com");

			System.out.println("deactivateUserTest: should have only printed that user2 is already deactivated");
			adminService.deactivateUser("user3@gmail.com");

			verify(userRep, times(1)).deactivateUser(any());
			verify(emailSender, times(1)).notifydeactivatedUser(any());
			verify(userRep, times(2)).findAll();
		} catch (EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(15)
	public void activateUserTest() {
		when(userRep.findByEmail(user2.getEmail())).thenReturn(Optional.of(user2));
		when(userRep.findByEmail(user3.getEmail())).thenReturn(Optional.of(user3));
		when(userRep.findAll()).thenReturn(users);

		assertFalse(user2.isActive());
		assertTrue(user3.isActive());

		try {
			adminService.activateUser("user3@gmail.com");

			System.out.println("activateUserTest: should have only printed that user3 is already activated");
			adminService.activateUser("user2@gmail.com");

			verify(userRep, times(1)).activateUser(any());
			verify(emailSender, times(1)).notifyActivatedUser(any());
			verify(userRep, times(2)).findAll();
		} catch (EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(16)
	public void updateCurrentCategoryTest() {
		when(categoryRep.getCurrent()).thenReturn(Optional.of(catRefua));
		when(categoryRep.findAll()).thenReturn(categories);

		adminService.updateCurrentCategory();

		verify(categoryRep, times(1)).getCurrent();
		verify(categoryRep, times(1)).findAll();
		verify(categoryRep, times(1)).updateCategoryCurrent(eq(true), anyLong());
		verify(categoryRep, times(1)).updateCategoryCurrent(eq(false), eq(catRefua.getId()));
	}

	@Test
	@Order(17)
	public void getAllCategoriesTest() {
		when(categoryRep.findAllOrderById()).thenReturn(categories);
		assertEquals(5, adminService.getAllCategories().size());

		verify(categoryRep, times(1)).findAllOrderById();
	}

	@Test
	@Order(18)
	public void getAllParashotTest() {
		when(parashaRep.findAll()).thenReturn(parashot);
		assertEquals(3, adminService.getAllParashot().size());

		verify(parashaRep, times(1)).findAll();
	}

	@Test
	@Order(19)
	public void findCurrentParashaTest() {
		when(parashaRep.findCurrent()).thenReturn(Optional.of(parasha1));
		assertEquals(parasha1, adminService.findCurrentParasha());

		verify(parashaRep, times(1)).findCurrent();
	}

	@Test
	@Order(20)
	public void findCurrentCategoryTest() {
		when(categoryRep.getCurrent()).thenReturn(Optional.of(catYeshua));
		assertEquals(catYeshua, adminService.findCurrentCategory());

		verify(categoryRep, times(1)).getCurrent();
	}

	@Test
	@Order(21)
	public void previewWeeklyTest() {
		// when(categoryRep.findById(3L)).thenReturn(Optional.empty());
		when(categoryRep.findById(2L)).thenReturn(Optional.of(catShidduchim));
		when(categoryRep.findById(5L)).thenReturn(Optional.of(catYeshua));
		when(categoryRep.findAll()).thenReturn(categories);

		when(davenforRep.findAllDavenforByCategory(eq(SHIDDUCHIM.toString()))).thenReturn(Collections.emptyList());
		when(davenforRep.findAllDavenforByCategory(eq(YESHUA_AND_PARNASSA.toString())))
				.thenReturn(Arrays.asList(dfYeshua1, dfYeshua2));

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.previewWeekly(new Weekly("Vayera", "Vayera - וירא", 3L, "nonexistent", "message"));
		});
		assertTrue(exception.getMessage().contains("category named nonexistent"));

		try {
			exception = assertThrows(EmptyInformationException.class, () -> {
				adminService.previewWeekly(new Weekly("Vayera", "וירא", 2L, "shidduchim", "message"));
			});
			assertTrue(exception.getMessage().contains("no names"));

			String html = adminService
					.previewWeekly(new Weekly("Vayechi", "ויחי", 5L, "YESHUA_AND_PARNASSA", "message 2"));

			assertTrue(html.contains("Vayechi"));
			assertTrue(html.contains("Yeshua and Parnassa"));
			assertTrue(html.contains("Next week Refua"));

			verify(categoryRep, times(1)).findAll(); // when printing good weekly, finds category of next week
			verify(davenforRep, times(2)).findAllDavenforByCategory(any());

		} catch (ObjectNotFoundException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(22)
	public void getAdminSettingsTest() {
		when(adminRep.getAdminByEmail(eq(adminEmail))).thenReturn(Optional.of(admin1));
		try {
			AdminSettings adminSettings = adminService.getAdminSettings(adminEmail);
			assertEquals(admin1.getEmail(), adminSettings.getEmail());
			assertEquals(7, adminSettings.getWaitBeforeDeletion());
			assertEquals(false, adminSettings.isNewNamePrompt());

			verify(adminRep, times(1)).getAdminByEmail(any());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
}
