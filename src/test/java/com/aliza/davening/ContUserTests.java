package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUA_AND_PARNASSA;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
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
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.rest.UserWebService;
import com.aliza.davening.security.AuthEntryPointJwt;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.security.UserDetailsServiceImpl;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.UserService;

@WebMvcTest(controllers = UserWebService.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContUserTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

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
	public static Category catYeshua = new Category(YESHUA_AND_PARNASSA, false, 180, 5);

	public static Davenfor dfRefua = new Davenfor(1, "user1@gmail.com", "Refua", "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshua1 = new Davenfor(2, "user1@gmail.com", "Yeshua_and_Parnassa", "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "user2@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshua2 = new Davenfor(4, "user2@gmail.com", "Yeshua_and_Parnassa", "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshua1, dfBanim, dfYeshua2);

	private final static String UNEXPECTED_E = "   ************* Attention: @Submitter controller test unexpected Exception: ";

	@Test
	@Order(1)
	public void testGetSubmitterDavenfors() {
		when(userService.getAllUserDavenfors("user1@gmail.com")).thenReturn(Arrays.asList(dfRefua, dfYeshua1));

		try {
			mockMvc.perform(get("/user/getmynames/{email}", "user1@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[0].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[1].id").value(2))
					.andExpect(jsonPath("$[1].nameEnglish").value("Moshe ben Sara"));

			verify(userService, times(1)).getAllUserDavenfors(eq("user1@gmail.com"));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(2)
	public void testAddDavenfor()
			throws EmptyInformationException, EmailException, IOException, ObjectNotFoundException {// TODO*: add tests
																									// for last 3
																									// exceptions
		// todo* - fix. check if still true: This exception should no longer be thrown.
		when(userService.addDavenfor(any(), eq("user1@gmail.com"))).thenThrow(new EmptyInformationException(
				"This category requires also a spouse name (English and Hebrew) to be submitted. "));

		String requestBodyPartialBanim = "{ \"email\": \"user3@gmail.com\", \"category\": \"BANIM\", \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";
		try {
			mockMvc.perform(post("/user/{email}", "user1@gmail.com").content(requestBodyPartialBanim)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("spouse name (English and Hebrew)")));

			when(userService.addDavenfor(any(), eq("user3@gmail.com"))).thenReturn(true);
			String requestBodyGood = "{ \"email\": \"user3@gmail.com\", \"category\": \"YESHUA_AND_PARNASSA\",  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

			mockMvc.perform(post("/user/{email}", "user3@gmail.com").content(requestBodyGood)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$").value("true"));

			verify(userService, times(2)).addDavenfor(any(), any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(3)
	public void testUpdateDavenfor() {
		String requestBody = "{ \"email\": \"user3@gmail.com\", \"category\" :\"YESHUA_AND_PARNASSA\",  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

		try {
			when(userService.updateDavenfor(any(), eq("user1@gmail.com"), eq(false))).thenReturn(dfRefua);
			mockMvc.perform(put("/user/updatename/{email}", "user1@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.nameEnglish").value("Avraham ben Sara"));

			when(userService.updateDavenfor(any(), eq("user2@gmail.com"), eq(false))).thenThrow(
					new EmptyInformationException("No information submitted regarding the name you wish to update. "));
			mockMvc.perform(put("/user/updatename/{email}", "user2@gmail.com").content("{ }")
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No information")));

			when(userService.updateDavenfor(any(), eq("user3@gmail.com"), eq(false)))
					.thenThrow(new ObjectNotFoundException("Name with id: 3"));
			mockMvc.perform(put("/user/updatename/{email}", "user3@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(userService.updateDavenfor(any(), eq("user4@gmail.com"), eq(false))).thenThrow(new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it."));
			mockMvc.perform(put("/user/updatename/{email}", "user4@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(userService, times(4)).updateDavenfor(any(), any(), eq(false));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(4)
	public void testExtendDavenfor() {
		try {
			mockMvc.perform(get("/user/extend/{davenforId}/{email}", 3L).param("email", "user1@gmail.com")
					.param("davenforId", "1")).andDo(print()).andExpect(status().isOk());

			when(userService.extendDavenfor(anyLong(), eq("user2@gmail.com")))
					.thenThrow(new EmptyInformationException("No associated email address was received. "));

			mockMvc.perform(get("/user/extend/{davenforId}", 3L).param("email", "user2@gmail.com")).andDo(print())
					.andExpect(status().isNoContent()).andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No associated email")));

			when(userService.extendDavenfor(anyLong(), eq("user3@gmail.com")))
					.thenThrow(new ObjectNotFoundException("Name with id: 3"));
			mockMvc.perform(get("/user/extend/{davenforId}", 3L).param("email", "user3@gmail.com")).andDo(print())
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(userService.extendDavenfor(anyLong(), eq("user4@gmail.com"))).thenThrow(new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it."));
			mockMvc.perform(get("/user/extend/{davenforId}", 3L).param("email", "user4@gmail.com")).andDo(print())
					.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(userService, times(4)).extendDavenfor(eq(3L), any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(5)
	public void testDeleteDavenfor() {
		try {
			when(userService.deleteDavenfor(1L, "user1@gmail.com", false))
					.thenReturn(Arrays.asList(dfYeshua1, dfBanim, dfYeshua2));

			mockMvc.perform(delete("/user/delete/{id}/{email}", "1", "user1@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[0].id").value(2)).andExpect(jsonPath("$[1].id").value(3))
					.andExpect(jsonPath("$[2].id").value(4))
					.andExpect(jsonPath("$[0].nameEnglish").value("Moshe ben Sara"))
					.andExpect(jsonPath("$[1].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[2].nameEnglish").value("Amram ben Shira"));

			when(userService.deleteDavenfor(2L, "user2@gmail.com", false))
					.thenThrow(new ObjectNotFoundException("Name with id 2"));

			mockMvc.perform(delete("/user/delete/{id}/{email}", "2", "user2@gmail.com")).andDo(print())
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(userService.deleteDavenfor(3L, "user3@gmail.com", false)).thenThrow(new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it."));

			mockMvc.perform(delete("/user/delete/{id}/{email}", 3L, "user3@gmail.com")).andDo(print())
					.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(userService, times(3)).deleteDavenfor(anyLong(), anyString(), anyBoolean());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(6)
	public void testFindAllCategories() {
		try {
			when(userService.getAllCategories())
					.thenReturn(Arrays.asList(catRefua, catBanim, catShidduchim, catSoldiers, catYeshua));

			mockMvc.perform(get("/user/categories")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(5)).andExpect(jsonPath("$[0]").value("refua"))
					.andExpect(jsonPath("$[1]").value("banim")).andExpect(jsonPath("$[4]").value("yeshua_and_parnassa"));

			when(userService.getAllCategories()).thenThrow(new ObjectNotFoundException("System categories"));

			mockMvc.perform(get("/user/categories")).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("System categories")));

			verify(userService, times(2)).getAllCategories();
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(7)
	public void testFindCategory() {
		try {
			when(userService.getCategory(1L)).thenReturn(catRefua);

			mockMvc.perform(get("/user/category/{id}", 1L)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$").value("refua"));

			when(userService.getCategory(6L)).thenThrow(new ObjectNotFoundException("Category with id: 6"));

			mockMvc.perform(get("/user/category/{id}", 6L)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Category with id")));

			verify(userService, times(2)).getCategory(anyLong());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
}
