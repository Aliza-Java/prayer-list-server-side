package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUAH;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davener;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.NoRelatedEmailException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.rest.AdminWebService;
import com.aliza.davening.security.AuthEntryPointJwt;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.security.UserDetailsServiceImpl;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.SubmitterService;
import com.aliza.davening.util_classes.AdminSettings;

@WebMvcTest(controllers = AdminWebService.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContAdminTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AdminService adminService;

	@MockBean
	private SubmitterService submitterService;

	@MockBean
	private EmailSender emailSender;

	@MockBean
	private UserDetailsService userDetailsService;

	@MockBean
	private UserDetailsServiceImpl userDetailsServiceImpl;

	@MockBean
	private AuthEntryPointJwt authEntryPointJwt;

	@MockBean
	private JwtUtils jwtUtils;

	public static Category catRefua = new Category(REFUA, true, 180, 1);
	public static Category catShidduchim = new Category(SHIDDUCHIM, false, 40, 2);
	public static Category catBanim = new Category(BANIM, false, 50, 3);
	public static Category catSoldiers = new Category(SOLDIERS, false, 180, 4);
	public static Category catYeshuah = new Category(YESHUAH, false, 180, 5);

	public static Davenfor dfRefua = new Davenfor(1, "sub1@gmail.com", "Refua", "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshuah1 = new Davenfor(2, "sub1@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "sub2@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshuah2 = new Davenfor(4, "sub2@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshuah1, dfBanim, dfYeshuah2);

	public static Davener davener1 = new Davener(1, "Israel", "davener1@gmail.com", null, false);
	public static Davener davener2 = new Davener(2, "Israel", "davener2@gmail.com", null, false);
	public static Davener davener3 = new Davener(3, "Israel", "davener3@gmail.com", null, true);
	public static List<Davener> daveners = Arrays.asList(davener1, davener2, davener3);

	public static Parasha parasha1 = new Parasha(1, "Bereshit", "בראשית", true);
	public static Parasha parasha2 = new Parasha(2, "Noach", "נח", false);
	public static Parasha parasha3 = new Parasha(3, "Lech Lecha", "לך-לך", false);
	public static List<Parasha> parashot = Arrays.asList(parasha1, parasha2, parasha3);

	private final static String UNEXPECTED_E = "   ************* Attention: @Admin controller test unexpected Exception: ";

	@Test
	@Order(1)
	public void testUpdateAdminSettingsOk() {
		try {
			String admin7 = "{\"email\": \"admin@gmail.com\", \"newNamePrompt\": false, \"waitBeforeDeletion\": 7}";

			when(adminService.updateAdmin(any())).thenReturn(true);

			mockMvc.perform(put("/admin/update").content(admin7).contentType(MediaType.APPLICATION_JSON)).andDo(print())
					.andExpect(status().isOk()).andExpect(content().string("true"));

			verify(adminService, times(1)).updateAdmin(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(2)
	public void testUpdateAdminSettingsNotFound() {
		try {
			String admin7 = "{\"email\": \"admin@gmail.com\", \"newNamePrompt\": false, \"waitBeforeDeletion\": 7}";

			when(adminService.updateAdmin(any())).thenThrow(new ObjectNotFoundException("Admin with id 8"));
			mockMvc.perform(put("/admin/update").content(admin7).contentType(MediaType.APPLICATION_JSON)).andDo(print())
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Admin with id")));

			verify(adminService, times(1)).updateAdmin(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(3)
	public void testUpdateAdminSettingsDbFailure() {
		try {
			String admin7 = "{\"email\": \"admin@gmail.com\", \"newNamePrompt\": false, \"waitBeforeDeletion\": 7}";

			when(adminService.updateAdmin(any()))
					.thenThrow(new DatabaseException("This admin email address is already in use."));
			mockMvc.perform(put("/admin/update").content(admin7).contentType(MediaType.APPLICATION_JSON)).andDo(print())
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.code").value("DATABASE_EXCEPTION"))
					.andExpect(jsonPath("$.messages[0]", containsString("already in use")));

			verify(adminService, times(1)).updateAdmin(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(4)
	public void testGetAdminSettings() {
		try {
			when(adminService.getAdminSettings(any())).thenReturn(new AdminSettings("admin5@gmail.com", true, 5));

			mockMvc.perform(get("/admin/settings/{email}", "admin5@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.email").value("admin5@gmail.com"))
					.andExpect(jsonPath("$.waitBeforeDeletion").value(5));

			when(adminService.getAdminSettings(any()))
					.thenThrow(new ObjectNotFoundException("Admin with email admin5@gmail.com"));
			mockMvc.perform(get("/admin/settings/{email}", "admin5@gmail.com")).andDo(print())
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Admin with email admin5@gmail.com")));

			verify(adminService, times(2)).getAdminSettings(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(5)
	public void testCheckPassword() {

		String requestBody = "{\"password\": \"pass1234\"}";

		try {
			when(adminService.checkPassword(any(), any())).thenReturn(true);

			mockMvc.perform(post("/admin/checkpass/{email}", "admin5@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$").value("true"));

			when(adminService.checkPassword(any(), any())).thenReturn(false);

			mockMvc.perform(post("/admin/checkpass/{email}", "admin6@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$").value("false"));

			when(adminService.checkPassword(any(), any()))
					.thenThrow(new ObjectNotFoundException("Admin with email admin7@gmail.com"));
			mockMvc.perform(post("/admin/checkpass/{email}", "admin6@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Admin with email")));

			verify(adminService, times(3)).checkPassword(any(), any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(6)
	public void testFindAllDavenfors() {
		try {
			when(adminService.getAllDavenfors()).thenReturn(davenfors);

			mockMvc.perform(get("/admin/davenfors")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(4)).andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[0].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[1].id").value(2))
					.andExpect(jsonPath("$[1].nameEnglish").value("Moshe ben Sara"));

			when(adminService.getAllDavenfors()).thenReturn(Collections.emptyList());

			mockMvc.perform(get("/admin/davenfors")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(0));

			verify(adminService, times(2)).getAllDavenfors();

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(7)
	public void testDeleteDavenfor() {
		try {
			when(adminService.deleteDavenfor(1L)).thenReturn(Arrays.asList(dfYeshuah1, dfBanim, dfYeshuah2));

			mockMvc.perform(delete("/admin/delete/{id}", 1)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].id").value(2))
					.andExpect(jsonPath("$[1].id").value(3)).andExpect(jsonPath("$[2].id").value(4))
					.andExpect(jsonPath("$[0].nameEnglish").value("Moshe ben Sara"))
					.andExpect(jsonPath("$[1].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[2].nameEnglish").value("Amram ben Shira"));

			when(adminService.deleteDavenfor(2L)).thenThrow(new ObjectNotFoundException("Name with id: 2"));

			mockMvc.perform(delete("/admin/delete/{id}", 2)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id:")));

			verify(adminService, times(2)).deleteDavenfor(anyLong());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(8)
	public void testFindAllDaveners() {
		try {
			when(adminService.getAllDaveners()).thenReturn(daveners);

			mockMvc.perform(get("/admin/daveners")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[0].email").value("davener1@gmail.com"))
					.andExpect(jsonPath("$[1].email").value("davener2@gmail.com"))
					.andExpect(jsonPath("$[2].email").value("davener3@gmail.com"));

			when(adminService.getAllDaveners()).thenReturn(Collections.emptyList());

			mockMvc.perform(get("/admin/daveners")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(0));

			verify(adminService, times(2)).getAllDaveners();

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(9)
	public void testCreateDavener() {
		Davener newDavener = new Davener(4, "Africa", "davener4@gmail.com", null, true);
		List<Davener> extendedDaveners = Arrays.asList(davener1, davener2, davener3, newDavener);
		assertEquals(4, extendedDaveners.size());

		String requestBody = "{\"id\": 4, \"country\": \"Africa\", \"email\": \"davener4@gmail.com\", \"whatsapp\" : null, \"active\" : true}";

		try {
			when(adminService.addDavener(any())).thenReturn(extendedDaveners);

			mockMvc.perform(post("/admin/davener").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4))
					.andExpect(jsonPath("$[0].email").value("davener1@gmail.com"))
					.andExpect(jsonPath("$[1].email").value("davener2@gmail.com"))
					.andExpect(jsonPath("$[2].email").value("davener3@gmail.com"))
					.andExpect(jsonPath("$[3].email").value("davener4@gmail.com"));

			newDavener.setEmail(null);
			when(adminService.addDavener(any())).thenThrow(
					new NoRelatedEmailException("Cannot enter this davener into the system.  No email associated. "));

			mockMvc.perform(post("/admin/davener").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No email associated.")));

			verify(adminService, times(2)).addDavener(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(10)
	public void testUpdateDavener() {
		try {
			String requestBody = "{\"id\": 1, \"country\": \"Australia\", \"email\": \"davener1@gmail.com\", \"whatsapp\" : null, \"active\" : true}";

			Davener updatedDavener1 = new Davener(davener1);
			updatedDavener1.setCountry("Australia");
			updatedDavener1.setId(1L);
			List<Davener> davenersToReturn = Arrays.asList(updatedDavener1, davener2, davener3);

			when(adminService.updateDavener(any())).thenReturn(davenersToReturn);
			mockMvc.perform(put("/admin/davener").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[0].country").value("Australia"))
					.andExpect(jsonPath("$[1].country").value("Israel"));

			when(adminService.updateDavener(any())).thenThrow(new ObjectNotFoundException("Davener with id 1"));
			mockMvc.perform(put("/admin/davener").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Davener with id 1")));

			verify(adminService, times(2)).updateDavener(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(11)
	public void testDeleteDavener() {
		try {
			when(adminService.deleteDavener(1L)).thenReturn(true);

			mockMvc.perform(delete("/admin/davener/{id}", 1L)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$").value("true"));

			when(adminService.deleteDavener(2L)).thenThrow(new ObjectNotFoundException("Name with id: 2"));

			mockMvc.perform(delete("/admin/davener/{id}", 2L)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id: 2")));

			verify(adminService, times(2)).deleteDavener(anyLong());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(12)
	public void testDisactivateDavener() {
		try {
			davener3.setActive(false);
			when(adminService.disactivateDavener("davener3@gmail.com")).thenReturn(daveners);

			mockMvc.perform(post("/admin/disactivate/{davenerEmail}", "davener3@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[0].email").value("davener1@gmail.com"))
					.andExpect(jsonPath("$[1].email").value("davener2@gmail.com"))
					.andExpect(jsonPath("$[2].email").value("davener3@gmail.com"))
					.andExpect(jsonPath("$[2].active").value("false"));

			when(adminService.disactivateDavener(""))
					.thenThrow(new EmptyInformationException("Recipient email address missing."));
			mockMvc.perform(post("/admin/disactivate/{davenerEmail}", "")).andDo(print()).andDo(print())
					.andExpect(status().isNotFound());

			// empty email doesn't even go to the mapping and gives NOT_FOUND
			verify(adminService, times(1)).disactivateDavener(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(13)
	public void testActivateDavener() {
		try {
			davener3.setActive(true);
			when(adminService.activateDavener("davener3@gmail.com")).thenReturn(daveners);

			mockMvc.perform(post("/admin/activate/{davenerEmail}", "davener3@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[0].email").value("davener1@gmail.com"))
					.andExpect(jsonPath("$[1].email").value("davener2@gmail.com"))
					.andExpect(jsonPath("$[2].email").value("davener3@gmail.com"))
					.andExpect(jsonPath("$[2].active").value("true"));

			davener2.setEmail("");
			when(adminService.activateDavener(""))
					.thenThrow(new EmptyInformationException("Recipient email address missing."));
			mockMvc.perform(post("/admin/activate/{davenerEmail}", "")).andDo(print()).andDo(print())
					.andExpect(status().isNotFound());

			// empty email doesn't even go to the mapping and gives NOT_FOUND
			verify(adminService, times(1)).activateDavener(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(14)
	public void testSendOutWeekly() {
		String requestBody = "{\"parashaName\": \"Noach\", \"categoryId\": 5, \"cName\": \"yeshuah\", \"message\" : null}";

		try {
			mockMvc.perform(post("/admin/weekly").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk()).andExpect(content().string("true"));

			doThrow(new EmptyInformationException("There are no names to daven for in this category. "))
					.when(emailSender).sendOutWeekly(any());
			mockMvc.perform(post("/admin/weekly").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("no names to daven for")));

			doThrow(new IOException("We are sorry, but something wrong happened. Please contact the admin."))
					.when(emailSender).sendOutWeekly(any());
			mockMvc.perform(post("/admin/weekly").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.code").value("SERVER_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("something wrong")));

			doThrow(new ObjectNotFoundException("Category not found")).when(emailSender).sendOutWeekly(any());
			mockMvc.perform(post("/admin/weekly").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Category not found")));

			verify(emailSender, times(4)).sendOutWeekly(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(15)
	public void testPreviewWeeklyOk() {
		String requestBody = "{\"parashaName\": \"Noach\", \"categoryId\": 5, \"cName\": \"yeshuah\", \"message\" : null}";
		try {
			when(adminService.previewWeekly(any())).thenReturn("This seems to work");

			mockMvc.perform(post("/admin/preview").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").value("This seems to work"));

			verify(adminService, times(1)).previewWeekly(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(16)
	public void testPreviewWeeklyEmptyInformation() {
		String requestBody = "{\"parashaName\": \"Noach\", \"categoryId\": 5, \"cName\": \"yeshuah\", \"message\" : null}";

		try {
			when(adminService.previewWeekly(any()))
					.thenThrow(new EmptyInformationException("There are no names to daven for in this category. "));

			mockMvc.perform(post("/admin/preview").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("no names to daven for")));

			verify(adminService, times(1)).previewWeekly(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(17)
	public void testPreviewWeeklyObjectNotFound() {
		String requestBody = "{\"parashaName\": \"Noach\", \"categoryId\": 5, \"cName\": \"yeshuah\", \"message\" : null}";

		try {
			when(adminService.previewWeekly(any())).thenThrow(new ObjectNotFoundException("category of id 5"));

			mockMvc.perform(post("/admin/preview").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("category of id 5")));

			verify(adminService, times(1)).previewWeekly(any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(18)
	public void testSendOutWeeklyFromEmail() {
		try {
			doNothing().when(emailSender).sendSimplifiedWeekly();
			mockMvc.perform(put("/admin/weeklylist")).andDo(print()).andExpect(status().isOk())
					.andExpect(content().string("true"));

			doThrow(new ObjectNotFoundException("current Parasha")).when(emailSender).sendSimplifiedWeekly();
			mockMvc.perform(put("/admin/weeklylist")).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("current Parasha not found")));

			doThrow(new EmptyInformationException("There are no names to daven for in this category."))
					.when(emailSender).sendSimplifiedWeekly();
			mockMvc.perform(put("/admin/weeklylist")).andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("no names to daven for")));

			doThrow(new IOException("We are sorry, but something wrong happened. Please contact the admin."))
					.when(emailSender).sendSimplifiedWeekly();
			mockMvc.perform(put("/admin/weeklylist")).andDo(print()).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.code").value("SERVER_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("something wrong")));

			verify(emailSender, times(4)).sendSimplifiedWeekly();

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(19)
	public void testUpdateNameByAdmin() {

		String requestBody = "{ \"email\": \"sub3@gmail.com\", \"category\" : {\"categoryName\": \"yeshuah\"},  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

		try {
			when(adminService.getAllDavenfors()).thenReturn(davenfors);
			mockMvc.perform(put("/admin/updatedavenfor").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4))
					.andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[0].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[1].id").value(2))
					.andExpect(jsonPath("$[1].nameEnglish").value("Moshe ben Sara"));

			when(submitterService.updateDavenfor(any(), eq(null), eq(true))).thenThrow(
					new EmptyInformationException("No information submitted regarding the name you wish to update. "));
			mockMvc.perform(put("/admin/updatedavenfor").content("{ }").contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No information")));

			when(submitterService.updateDavenfor(any(), eq(null), eq(true)))
					.thenThrow(new ObjectNotFoundException("Name with id: 3"));
			mockMvc.perform(put("/admin/updatedavenfor").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			// permission exception can't ever be thrown from updateNameByAdmin()

			verify(submitterService, times(3)).updateDavenfor(any(), eq(null), eq(true));
			verify(adminService, times(1)).getAllDavenfors();

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(20)
	public void testUpdateDavenfor() {

		String requestBody = "{ \"submitterEmail\": \"sub3@gmail.com\", \"category\" : {\"categoryName\": \"yeshuah\"},  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

		try {
			when(submitterService.updateDavenfor(any(), eq("sub3@gmail.com"), eq(false))).thenReturn(dfYeshuah1);
			mockMvc.perform(put("/admin/updatename/{email}", "sub3@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.nameEnglish").value("Moshe ben Sara"))
					.andExpect(jsonPath("$.submitterEmail").value("sub1@gmail.com"));

			when(submitterService.updateDavenfor(any(), eq("sub3@gmail.com"), eq(false))).thenThrow(
					new EmptyInformationException("No information submitted regarding the name you wish to update. "));
			mockMvc.perform(put("/admin/updatename/{email}", "sub3@gmail.com").content("{ }")
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No information")));

			when(submitterService.updateDavenfor(any(), eq("sub3@gmail.com"), eq(false)))
					.thenThrow(new ObjectNotFoundException("Name with id: 3"));
			mockMvc.perform(put("/admin/updatename/{email}", "sub3@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(submitterService.updateDavenfor(any(), eq("different@gmail.com"), eq(false)))
					.thenThrow(new PermissionException(
							"This name is registered under a different email address.  You do not have the permission to update it."));
			mockMvc.perform(put("/admin/updatename/{email}", "different@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(submitterService, times(4)).updateDavenfor(any(), any(), eq(false));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(21)
	public void testSendOutUrgent() {
		String requestBody = "{ \"submitterEmail\": \"sub3@gmail.com\", \"category\" : {\"categoryName\": \"yeshuah\"},  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";
		String incompleteDf = "{ \"submitterEmail\": \"sub3@gmail.com\", \"category\" : {\"categoryName\": \"yeshuah\"},  \"nameEnglish\": \" \", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

		try {
			doNothing().when(emailSender).sendUrgentEmail(any());
			mockMvc.perform(post("/admin/urgent").content(requestBody).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").value("true"));

			doThrow(new EmptyInformationException("The name you submitted for davening is incomplete."))
					.when(emailSender).sendUrgentEmail(any());
			mockMvc.perform(post("/admin/urgent").content(incompleteDf).contentType(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("is incomplete")));

			verify(emailSender, times(2)).sendUrgentEmail(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(22)
	public void testFindAllCategories() {
		when(adminService.getAllCategories())
				.thenReturn(Arrays.asList(catRefua, catBanim, catShidduchim, catSoldiers, catYeshuah));
		try {
			mockMvc.perform(get("/admin/categories")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(5)).andExpect(jsonPath("$[0]").value("refua"))
					.andExpect(jsonPath("$[4]").value("yeshuah"));

			when(adminService.getAllCategories()).thenReturn(Collections.emptyList());

			mockMvc.perform(get("/admin/categories")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(0));

			verify(adminService, times(2)).getAllCategories();

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(23)
	public void testFindAllParashot() {
		when(adminService.getAllParashot()).thenReturn(parashot);
		try {
			mockMvc.perform(get("/admin/parashot")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[1].englishName").value("Noach"))
					.andExpect(jsonPath("$[2].current").value("false"));

			when(adminService.getAllParashot()).thenReturn(Collections.emptyList());

			mockMvc.perform(get("/admin/parashot")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(0));

			verify(adminService, times(2)).getAllParashot();

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
}
