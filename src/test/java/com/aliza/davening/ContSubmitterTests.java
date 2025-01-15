package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUAH;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.rest.SubmitterWebService;
import com.aliza.davening.security.AuthEntryPointJwt;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.security.UserDetailsServiceImpl;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.SubmitterService;

@WebMvcTest(controllers = SubmitterWebService.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContSubmitterTests {

	@Autowired
	private MockMvc mockMvc;

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

	public static Davenfor dfRefua = new Davenfor(1, "sub1@gmail.com", catRefua, "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshuah1 = new Davenfor(2, "sub1@gmail.com", catYeshuah, "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "sub2@gmail.com", catBanim, "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshuah2 = new Davenfor(4, "sub2@gmail.com", catYeshuah, "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshuah1, dfBanim, dfYeshuah2);

	private final static String UNEXPECTED_E = "   ************* Attention: @Submitter controller test unexpected Exception: ";

	@Test
	@Order(1)
	public void testGetSubmitterDavenfors() {
		when(submitterService.getAllSubmitterDavenfors("sub1@gmail.com"))
				.thenReturn(Arrays.asList(dfRefua, dfYeshuah1));

		try {
			mockMvc.perform(get("/sub/getmynames/{email}", "sub1@gmail.com")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1))
					.andExpect(jsonPath("$[0].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[1].id").value(2))
					.andExpect(jsonPath("$[1].nameEnglish").value("Moshe ben Sara"));

			verify(submitterService, times(1)).getAllSubmitterDavenfors(eq("sub1@gmail.com"));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(2)
	public void testAddDavenfor() throws EmptyInformationException {
		when(submitterService.addDavenfor(any(), eq("sub1@gmail.com"))).thenThrow(new EmptyInformationException(
				"This category requires also a spouse name (English and Hebrew) to be submitted. "));

		String requestBodyPartialBanim = "{ \"email\": \"sub3@gmail.com\", \"category\": {\"cname\": \"BANIM\"}, \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";
		try {
			mockMvc.perform(post("/sub/{email}", "sub1@gmail.com").content(requestBodyPartialBanim)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("spouse name (English and Hebrew)")));

			when(submitterService.addDavenfor(any(), eq("sub2@gmail.com"))).thenReturn(dfYeshuah2);
			String requestBodyGood = "{ \"email\": \"sub3@gmail.com\", \"category\":{\"cname\": \"YESHUAH\"},  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

			mockMvc.perform(post("/sub/{email}", "sub2@gmail.com").content(requestBodyGood)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(4)).andExpect(jsonPath("$.nameEnglish").value("Amram ben Shira"));

			verify(submitterService, times(2)).addDavenfor(any(), any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(3)
	public void testUpdateDavenfor() {
		String requestBody = "{ \"email\": \"sub3@gmail.com\", \"category\":{\"cname\": \"YESHUAH\"},  \"nameEnglish\": \"Moshe ben Sara\", \"nameHebrew\": \"משה בן שרה\", \"submitterToReceive\": true }";

		try {
			when(submitterService.updateDavenfor(any(), eq("sub1@gmail.com"), eq(false))).thenReturn(dfRefua);
			mockMvc.perform(put("/sub/updatename/{email}", "sub1@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.nameEnglish").value("Avraham ben Sara"));

			when(submitterService.updateDavenfor(any(), eq("sub2@gmail.com"), eq(false))).thenThrow(
					new EmptyInformationException("No information submitted regarding the name you wish to update. "));
			mockMvc.perform(put("/sub/updatename/{email}", "sub2@gmail.com").content("{ }")
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent())
					.andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No information")));

			when(submitterService.updateDavenfor(any(), eq("sub3@gmail.com"), eq(false)))
					.thenThrow(new ObjectNotFoundException("Name with id: 3"));
			mockMvc.perform(put("/sub/updatename/{email}", "sub3@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(submitterService.updateDavenfor(any(), eq("sub4@gmail.com"), eq(false)))
					.thenThrow(new PermissionException(
							"This name is registered under a different email address.  You do not have the permission to update it."));
			mockMvc.perform(put("/sub/updatename/{email}", "sub4@gmail.com").content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(submitterService, times(4)).updateDavenfor(any(), any(), eq(false));
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(4)
	public void testExtendDavenfor() {
		try {
			mockMvc.perform(get("/sub/extend/{davenforId}", 3L).param("email", "sub1@gmail.com")).andDo(print())
					.andExpect(status().isOk());

			when(submitterService.extendDavenfor(anyLong(), eq("sub2@gmail.com")))
					.thenThrow(new EmptyInformationException("No associated email address was received. "));

			mockMvc.perform(get("/sub/extend/{davenforId}", 3L).param("email", "sub2@gmail.com")).andDo(print())
					.andExpect(status().isNoContent()).andExpect(jsonPath("$.code").value("EMPTY_INFORMATION"))
					.andExpect(jsonPath("$.messages[0]", containsString("No associated email")));

			when(submitterService.extendDavenfor(anyLong(), eq("sub3@gmail.com")))
					.thenThrow(new ObjectNotFoundException("Name with id: 3"));
			mockMvc.perform(get("/sub/extend/{davenforId}", 3L).param("email", "sub3@gmail.com")).andDo(print())
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(submitterService.extendDavenfor(anyLong(), eq("sub4@gmail.com"))).thenThrow(new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to update it."));
			mockMvc.perform(get("/sub/extend/{davenforId}", 3L).param("email", "sub4@gmail.com")).andDo(print())
					.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(submitterService, times(4)).extendDavenfor(eq(3L), any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(5)
	public void testDeleteDavenfor() {
		try {
			when(submitterService.deleteDavenfor(1L, "sub1@gmail.com"))
					.thenReturn(Arrays.asList(dfYeshuah1, dfBanim, dfYeshuah2));

			mockMvc.perform(delete("/sub/delete/{id}/{email}", "1", "sub1@gmail.com")).andDo(print())
					.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
					.andExpect(jsonPath("$[0].id").value(2)).andExpect(jsonPath("$[1].id").value(3))
					.andExpect(jsonPath("$[2].id").value(4))
					.andExpect(jsonPath("$[0].nameEnglish").value("Moshe ben Sara"))
					.andExpect(jsonPath("$[1].nameEnglish").value("Avraham ben Sara"))
					.andExpect(jsonPath("$[2].nameEnglish").value("Amram ben Shira"));

			when(submitterService.deleteDavenfor(2L, "sub2@gmail.com"))
					.thenThrow(new ObjectNotFoundException("Name with id 2"));

			mockMvc.perform(delete("/sub/delete/{id}/{email}", "2", "sub2@gmail.com")).andDo(print())
					.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("Name with id")));

			when(submitterService.deleteDavenfor(3L, "sub3@gmail.com")).thenThrow(new PermissionException(
					"This name is registered under a different email address.  You do not have the permission to delete it."));

			mockMvc.perform(delete("/sub/delete/{id}/{email}", 3L, "sub3@gmail.com")).andDo(print())
					.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("LOGIN_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("do not have the permission")));

			verify(submitterService, times(3)).deleteDavenfor(anyLong(), any());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(6)
	public void testFindAllCategories() {
		try {
			when(submitterService.getAllCategories())
					.thenReturn(Arrays.asList(catRefua, catBanim, catShidduchim, catSoldiers, catYeshuah));

			mockMvc.perform(get("/sub/categories")).andDo(print()).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(5)).andExpect(jsonPath("$[0]").value("refua"))
					.andExpect(jsonPath("$[1]").value("banim"))
					.andExpect(jsonPath("$[4]").value("yeshuah"));

			when(submitterService.getAllCategories()).thenThrow(new ObjectNotFoundException("System categories"));

			mockMvc.perform(get("/sub/categories")).andDo(print()).andExpect(status().isNotFound())
					.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
					.andExpect(jsonPath("$.messages[0]", containsString("System categories")));

			verify(submitterService, times(2)).getAllCategories();
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
	
	@Test
	@Order(7)
	public void testFindCategory() {
		try {
			when(submitterService.getCategory(1L)).thenReturn(catRefua);
			
			mockMvc.perform(get("/sub/category/{id}", 1L)).andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value("refua"));
			
			when(submitterService.getCategory(6L)).thenThrow(new ObjectNotFoundException("Category with id: 6"));
			
			mockMvc.perform(get("/sub/category/{id}", 6L)).andDo(print())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("OBJECT_NOT_FOUND_ERROR"))
			.andExpect(jsonPath("$.messages[0]", containsString("Category with id")));
			
			verify(submitterService, times(2)).getCategory(anyLong());
		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
}
