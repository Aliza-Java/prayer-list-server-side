package com.aliza.davening;

import static com.aliza.davening.entities.CategoryType.BANIM;
import static com.aliza.davening.entities.CategoryType.REFUA;
import static com.aliza.davening.entities.CategoryType.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryType.SOLDIERS;
import static com.aliza.davening.entities.CategoryType.YESHUAH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davener;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.NoRelatedEmailException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.repositories.SubmitterRepository;
import com.aliza.davening.security.LoginRequest;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.util_classes.AdminSettings;
import com.aliza.davening.util_classes.Weekly;

import jakarta.mail.MessagingException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceAdminTests {

	@Autowired
	private AdminService adminService;
	
	@MockBean
	private DavenforRepository davenforRep;

	@MockBean
	private CategoryRepository categoryRep;

	@MockBean
	private SubmitterRepository submitterRep;

	@MockBean
	private AdminRepository adminRep;

	@MockBean
	private ParashaRepository parashaRep;

	@MockBean
	private DavenerRepository davenerRep;

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
	public static Category catYeshuah = new Category(YESHUAH, false, 180, 5);
	public static List<Category> categories = Arrays.asList(catRefua, catShidduchim, catBanim, catSoldiers, catYeshuah);

	public static Admin admin1 = new Admin(1, adminEmail, adminPass, false, 7);
	public static Admin admin2 = new Admin(2, "admin2@gmail.com", null, false, 7);
	public static Admin admin3 = new Admin(3, "admin3@gmail.com", null, false, 7);

	public static Davener davener1 = new Davener(1, "Israel", "davener1@gmail.com", null, false);
	public static Davener davener2 = new Davener(2, "Israel", "davener2@gmail.com", null, false);
	public static Davener davener3 = new Davener(3, "Israel", "davener3@gmail.com", null, true);
	public static List<Davener> daveners = Arrays.asList(davener1, davener2, davener3);

	public static Submitter sub1 = new Submitter("sub1@gmail.com");
	public static Submitter sub2 = new Submitter("sub2@gmail.com");
	public static List<Submitter> submitters = Arrays.asList(sub1, sub2);

	public static Parasha parasha1 = new Parasha(1, "Bereshit", "בראשית", true);
	public static Parasha parasha2 = new Parasha(2, "Noach", "נח", false);
	public static Parasha parasha3 = new Parasha(3, "Lech Lecha", "לך-לך", false);
	public static List<Parasha> parashot = Arrays.asList(parasha1, parasha2, parasha3);

	public static Davenfor dfRefua = new Davenfor(1, "sub1@gmail.com", catRefua, "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshuah1 = new Davenfor(4, "sub1@gmail.com", catYeshuah, "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "sub2@gmail.com", catBanim, "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshuah2 = new Davenfor(4, "sub2@gmail.com", catYeshuah, "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshuah1, dfBanim, dfYeshuah2);

	private final static String UNEXPECTED_E = "   ************* Attention: @Admin service test unexpected Exception: ";

	@BeforeAll
	private void baseTest() {
		
		//TODO: 
		//in service tests - fix base test, make generals.commit all. (maybe later)

		// when(submitterRep.findByEmail(submitterEmail)).thenReturn(new
		// Submitter(submitterEmail));

		// TODO: when email works enable real emailing through here (or through email
		// service tests)
		
		when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(Optional.of(catShidduchim));
		when(categoryRep.findByCname(BANIM)).thenReturn(Optional.of(catBanim));
		when(categoryRep.findByCname(REFUA)).thenReturn(Optional.of(catRefua));
		when(categoryRep.findByCname(YESHUAH)).thenReturn(Optional.of(catYeshuah));
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
		when(adminRep.findById(anyLong())).thenReturn(Optional.empty());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.updateAdmin(new AdminSettings());
		});
		assertTrue(exception.getMessage().contains("id"));

		List<Admin> admins = Arrays.asList(admin1, admin2, admin3);

		when(adminRep.findAll()).thenReturn(admins);

		when(adminRep.findById(1L)).thenReturn(Optional.of(admin1));
		exception = assertThrows(DatabaseException.class, () -> {
			adminService.updateAdmin(new AdminSettings("admin2@gmail.com", true, 8));
		});
		assertTrue(exception.getMessage().contains("email"));

		try {
			assertTrue(adminService.updateAdmin(new AdminSettings("newAdmin@gmail.com", false, 8)));
		} catch (ObjectNotFoundException | DatabaseException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(adminRep, times(3)).findById(any());
		verify(adminRep, times(2)).findAll();
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
	public void getAllDavenersTest() {
		when(davenerRep.findAll()).thenReturn(daveners);
		assertEquals(3, adminService.getAllDaveners().size());

		verify(davenerRep, times(1)).findAll();
	}

	@Test
	@Order(7)
	public void addDavenerTest() {
		davener1.setEmail(null);
		Exception exception = assertThrows(NoRelatedEmailException.class, () -> {
			adminService.addDavener(davener1);
		});
		assertTrue(exception.getMessage().contains("No email"));

		when(davenerRep.findByEmail("davener2@gmail.com")).thenReturn(Optional.of(davener2));
		when(davenerRep.findAll()).thenReturn(daveners);
		davener2.setActive(false);
		try {
			assertEquals(3, adminService.addDavener(davener2).size()); // should return 3 daveners
			assertTrue(davener2.isActive());
		} catch (NoRelatedEmailException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		when(davenerRep.findByEmail("newDavener@gmail.com")).thenReturn(null);
		davener3.setActive(false);
		try {
			assertEquals(3, adminService.addDavener(davener3).size()); // should return 3 daveners
			assertFalse(davener3.isActive());
		} catch (NoRelatedEmailException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(davenerRep, times(2)).findByEmail(any());
		verify(davenerRep, times(2)).findAll();

		// returning to original values
		davener2.setActive(false);
		davener3.setActive(true);
	}

	@Test
	@Order(8)
	public void getDavenerTest() {
		when(davenerRep.findById(4L)).thenReturn(Optional.empty());
		when(davenerRep.findById(3L)).thenReturn(Optional.of(davener3));

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.getDavener(4L);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			assertEquals(davener3, adminService.getDavener(3L));
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(davenerRep, times(2)).findById(anyLong());
	}

	@Test
	@Order(9)
	public void updateDavenerTest() {
		when(davenerRep.findById(1L)).thenReturn(Optional.empty());
		when(davenerRep.findById(3L)).thenReturn(Optional.of(davener3));
		when(davenerRep.findAll()).thenReturn(daveners);

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.updateDavener(davener1);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			assertEquals(3, adminService.updateDavener(davener3).size());
		} catch (ObjectNotFoundException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(davenerRep, times(2)).findById(anyLong());
		verify(davenerRep, times(1)).save(any());
		verify(davenerRep, times(1)).findAll();
	}

	@Test
	@Order(10)
	public void deleteDavenerTest() {
		doThrow(EmptyResultDataAccessException.class).when(davenerRep).deleteById(4L);
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.deleteDavener(4L);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			assertTrue(adminService.deleteDavener(3L));
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(davenerRep, times(2)).deleteById(anyLong());
	}

	@Test
	@Order(11)
	public void getAllSubmittersTest() {
		when(submitterRep.findAll()).thenReturn(submitters);
		assertEquals(2, adminService.getAllSubmitters().size());

		verify(submitterRep, times(1)).findAll();
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

		when(davenforRep.findById(3L)).thenReturn(Optional.of(dfYeshuah1));
		when(davenforRep.findAll()).thenReturn(davenfors);
		try {
			assertEquals(4, adminService.deleteDavenfor(3L).size());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

		verify(davenforRep, times(2)).findById(anyLong());
		verify(davenforRep, times(1)).deleteById(anyLong());
		verify(davenforRep, times(1)).findAll();
	}

	@Test
	@Order(14)
	public void disactivateDavenerTest() {
		when(davenerRep.findByEmail(davener2.getEmail())).thenReturn(Optional.of(davener2));
		when(davenerRep.findByEmail(davener3.getEmail())).thenReturn(Optional.of(davener3));
		when(davenerRep.findAll()).thenReturn(daveners);

		assertFalse(davener2.isActive());
		assertTrue(davener3.isActive());

		try {
			adminService.disactivateDavener("davener2@gmail.com");

			System.out
					.println("disactivateDavenerTest: should have only printed that davener2 is already disactivated");
			adminService.disactivateDavener("davener3@gmail.com");

			verify(davenerRep, times(1)).disactivateDavener(any());
			verify(emailSender, times(1)).notifyDisactivatedDavener(any());
			verify(davenerRep, times(2)).findAll();
		} catch (EmailException | DatabaseException | ObjectNotFoundException | EmptyInformationException
				| MessagingException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(15)
	public void activateDavenerTest() {
		when(davenerRep.findByEmail(davener2.getEmail())).thenReturn(Optional.of(davener2));
		when(davenerRep.findByEmail(davener3.getEmail())).thenReturn(Optional.of(davener3));
		when(davenerRep.findAll()).thenReturn(daveners);

		assertFalse(davener2.isActive());
		assertTrue(davener3.isActive());

		try {
			adminService.activateDavener("davener3@gmail.com");

			System.out.println("activateDavenerTest: should have only printed that davener3 is already activated");
			adminService.activateDavener("davener2@gmail.com");

			verify(davenerRep, times(1)).activateDavener(any());
			verify(emailSender, times(1)).notifyActivatedDavener(any());
			verify(davenerRep, times(2)).findAll();
		} catch (EmailException | DatabaseException | ObjectNotFoundException | EmptyInformationException
				| MessagingException e) {
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
		when(categoryRep.getCurrent()).thenReturn(Optional.of(catYeshuah));
		assertEquals(catYeshuah, adminService.findCurrentCategory());

		verify(categoryRep, times(1)).getCurrent();
	}

	@Test
	@Order(21)
	public void previewWeeklyTest() {
		when(categoryRep.findById(3L)).thenReturn(Optional.empty());
		when(categoryRep.findById(2L)).thenReturn(Optional.of(catShidduchim));
		when(categoryRep.findById(5L)).thenReturn(Optional.of(catYeshuah));
		when(categoryRep.findAll()).thenReturn(categories);

		when(davenforRep.findAllDavenforByCategory(eq(SHIDDUCHIM))).thenReturn(Collections.emptyList());
		when(davenforRep.findAllDavenforByCategory(eq(YESHUAH))).thenReturn(Arrays.asList(dfYeshuah1, dfYeshuah2));

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			adminService.previewWeekly(new Weekly("Vayera", 3L, catBanim, "message"));
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			exception = assertThrows(EmptyInformationException.class, () -> {
				adminService.previewWeekly(new Weekly("Vayera", 2L, catShidduchim, "message"));
			});
			assertTrue(exception.getMessage().contains("no names"));

			String html = adminService.previewWeekly(new Weekly("Vayechi", 5L, catYeshuah, "message 2"));

			assertTrue(html.contains("Vayechi"));
			assertTrue(html.contains("YESHUAH"));
			assertTrue(html.contains("Next week:  REFUA"));

			verify(categoryRep, times(3)).findById(anyLong());
			verify(categoryRep, times(1)).findAll();
			verify(davenforRep, times(2)).findAllDavenforByCategory(any());

		} catch (ObjectNotFoundException | IOException | DatabaseException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
	
	@Test
	@Order(22)
	public void getAdminSettingsTest()
	{
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
