package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUAH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.entities.User;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.UserRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.services.EmailSender;

//testing with test-DB
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
		"spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // annotation to force Spring to create a fresh
																	// application context for each test class.
@Transactional // ensures the actions will be rolled back after every test
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {
	// Integration tests - checking communication with all Repositories, all CRUD
	// actions, and functioning of both Submitter and Admin services.

	@MockBean
	private AuthenticationManager authenticationManager;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	ParashaRepository parashaRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	AdminRepository adminRepository;

	@MockBean
	EmailSender emailSender;

	@LocalServerPort
	private int port;

	public static Category catRefua = new Category(REFUA, true, 180, 1);
	public static Category catShidduchim = new Category(SHIDDUCHIM, false, 40, 2);
	public static Category catBanim = new Category(BANIM, false, 50, 3);
	public static Category catSoldiers = new Category(SOLDIERS, false, 180, 4);
	public static Category catYeshuah = new Category(YESHUAH, false, 180, 5);

	public static Davenfor dfRefua = new Davenfor(1, "user1@gmail.com", "Refua", "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshuah1 = new Davenfor(2, "user1@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "user2@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshuah2 = new Davenfor(4, "user2@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshuah1, dfBanim, dfYeshuah2);

	public static User user1 = new User(1, null, "user1@gmail.com", "Israel", null, null, false);
	public static User user2 = new User(2, null, "user2@gmail.com", "Israel", null, null, false);
	public static User user3 = new User(3, null, "user3@gmail.com", "Israel", null, null, true);
	public static List<User> users = Arrays.asList(user1, user2, user3);
	
	public static Parasha parasha1 = new Parasha(1, "Bereshit", "בראשית", true);
	public static Parasha parasha2 = new Parasha(2, "Noach", "נח", false);
	public static Parasha parasha3 = new Parasha(3, "Lech Lecha", "לך-לך", false);
	public static List<Parasha> parashot = Arrays.asList(parasha1, parasha2, parasha3);

	private final static String UNEXPECTED_E = "   ************* Attention: @Integration test unexpected Exception: ";

	@BeforeAll
	void setupOnce() {
		categoryRepository.save(catRefua);
		categoryRepository.save(catShidduchim);
		categoryRepository.save(catBanim);
		categoryRepository.save(catSoldiers);
		categoryRepository.save(catYeshuah);

		System.out.println(categoryRepository.findAll());

		adminRepository.save(new Admin(1L, "admin1@gmail.com", "pass1", true, 7));
		adminRepository.save(new Admin(2L, "admin2@gmail.com", "pass2", true, 8));
		adminRepository.save(new Admin(3L, "admin3@gmail.com", "pass3", true, 9));

		parashaRepository.save(new Parasha(1, "Bereshit", "בראשית", true));
		parashaRepository.save(new Parasha(2, "Noach", "נח", false));
		parashaRepository.save(new Parasha(3, "Lech Lecha", "לך-לך", false));

		userRepository.save(user1);
		userRepository.save(user2);
		userRepository.save(user3);

		davenforRepository.save(new Davenfor(1, "user1@gmail.com", "Refua", "אברהם בן שרה", "Avraham ben Sara", null,
				null, true, null, null, null, null, null));
		davenforRepository.save(new Davenfor(2, "user1@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara", null, null,
				true, null, null, null, null, null));
		davenforRepository.save(new Davenfor(3, "user2@gmail.com", "Banim", "יצחק בן שרה", "Yitzchak ben Sara",
				"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null));
		davenforRepository.save(new Davenfor(4, "user2@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira", null,
				null, true, null, null, null, null, null));
		davenforRepository.save(new Davenfor(5, "user2@gmail.com", "Banim", "יוסף בן שירה", "Yosef ben Shira", null,
				null, true, null, null, null, null, null));
	}

	@Test
	@Order(1)
	public void testH2ConsoleAvailability() {
		System.out.println("H2 Console available at: http://localhost:" + port + "/h2-console");
		System.out.println(categoryRepository.findAll());
	}

	@Test
	@Order(2)
	public void testFindAllParashot() {
		try {
			mockMvc.perform(get("/admin/parashot")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[0].englishName").value("Bereshit"))
					.andExpect(jsonPath("$[0].current").value("true"))
					.andExpect(jsonPath("$[2].current").value("false"));

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(3)
	public void testFindAllusers() {
		try {
			mockMvc.perform(get("/admin/users")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].active").value("false"))
					.andExpect(jsonPath("$[2].active").value("true"))
					.andExpect(jsonPath("$[0].email").value("user1@gmail.com"))
					.andExpect(jsonPath("$[1].email").value("user2@gmail.com"))
					.andExpect(jsonPath("$[2].email").value("user3@gmail.com"));

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(4)
	public void testGetSubmitterNames() {
		try {
			mockMvc.perform(get("/user/getmynames/{email}", "user1@gmail.com")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[1].nameEnglish").value("Moshe ben Sara"));

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(5)
	public void testGetAdminSettings() {
		try {
			mockMvc.perform(get("/admin/settings/{email}", "admin2@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.email").value("admin2@gmail.com"))
					.andExpect(jsonPath("$.waitBeforeDeletion").value("8"));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(6)
	public void testFindAllCategories() {
		try {
			mockMvc.perform(get("/admin/categories")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(5)).andExpect(jsonPath("$[0]").value("refua"))
					.andExpect(jsonPath("$[4]").value("yeshuah"));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(7)
	public void testDeleteDavenfor() {
		List<Davenfor> davenfors = davenforRepository.findAll();
		System.out.println(davenfors);
		try {
			mockMvc.perform(delete("/user/delete/{id}/{email}", 3L, "user2@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].nameEnglish").value("Amram ben Shira"))
					.andExpect(jsonPath("$[1].id").value("5"));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(8)
	public void testActivateuser() {
		try {
			doNothing().when(emailSender).notifyActivatedUser("user2@gmail.com");
			assertEquals("user2@gmail.com", userRepository.getOne(2L).getEmail());
			assertFalse(userRepository.getOne(2L).isActive());

			mockMvc.perform(post("/admin/activate/{userEmail}", "user2@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[1].active").value("true"));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(9)
	public void testAddDavenfor() {
		String requestBody = "{ \"email\": \"user3@gmail.com\", \"category\": \"YESHUAH\",  \"nameEnglish\": \"Yossi ben Sara\", \"nameHebrew\": \"יוסי בן שרה\", \"submitterToReceive\": true }";

		try {
			mockMvc.perform(
					post("/user/{email}", "user3@gmail.com").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.userEmail").value("user3@gmail.com"))
					.andExpect(jsonPath("$.nameEnglish").value("Yossi ben Sara"));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(10)
	public void testUpdateAdminSettings() {
		assertEquals(9, adminRepository.getAdminByEmail("admin3@gmail.com").get().getWaitBeforeDeletion());
		String adminRequestBody = "{\"email\": \"admin3@gmail.com\", \"newNamePrompt\": false, \"waitBeforeDeletion\": 77}";

		try {
			mockMvc.perform(put("/admin/update").content(adminRequestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk());

			assertEquals(77, adminRepository.getAdminByEmail("admin3@gmail.com").get().getWaitBeforeDeletion());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
}