package com.aliza.davening;

import static com.aliza.davening.entities.CategoryType.BANIM;
import static com.aliza.davening.entities.CategoryType.REFUA;
import static com.aliza.davening.entities.CategoryType.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryType.SOLDIERS;
import static com.aliza.davening.entities.CategoryType.YESHUAH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.mockito.Mockito.*;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.SubmitterService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubmitterServiceTests {

	@Autowired
	private SubmitterService submitterService;

	@MockBean
	private DavenforRepository davenforRep;

	@MockBean
	private CategoryRepository categoryRep;

	@MockBean
	private SubmitterRepository submitterRep;

	@MockBean
	private AdminRepository adminRep;

	@MockBean
	private EmailSender emailSender;

	static String submitterEmail = "sub.email@gmail.com";

	public static Category catRefua = new Category(REFUA, true, 180, 1);
	public static Category catShidduchim = new Category(SHIDDUCHIM, false, 40, 2);
	public static Category catBanim = new Category(BANIM, false, 50, 3);
	public static Category catSoldiers = new Category(SOLDIERS, false, 180, 4);
	public static Category catYeshuah = new Category(YESHUAH, false, 180, 5);
	
	private final static String UNEXPECTED_E = "   ************* Attention: @Submitter service test unexpected Exception: ";

	@BeforeAll
	private void baseTest() {
		when(submitterRep.findByEmail(submitterEmail)).thenReturn(new Submitter(submitterEmail));

		// TODO: when email works enable real emailing through here (or through email
		// service tests)
		try {
			when(emailSender.sendEmail(anyString(), // subject
					anyString(), // text
					anyString(), // to,
					any(), // bcc,
					any(), // attachment,
					anyString())) // attachmentName
							.thenReturn(true);
		} catch (MessagingException | EmailException e) {
			// do nothing
		}

		when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(catShidduchim);
		when(categoryRep.findByCname(BANIM)).thenReturn(catBanim);
		when(categoryRep.findByCname(REFUA)).thenReturn(catRefua);
		when(categoryRep.findByCname(YESHUAH)).thenReturn(catYeshuah);
		when(categoryRep.findByCname(SOLDIERS)).thenReturn(catSoldiers);
	}

	@Test
	public void getAllSubmitterDavenforsTest() {
		try {

			// service returns davenfors fetched from repository
			when(davenforRep.findAllDavenforBySubmitterEmail(submitterEmail)).thenReturn(getDfList());
			List<Davenfor> dfs = (submitterService.getAllSubmitterDavenfors(submitterEmail));
			assertTrue(dfs.size() == 3);

			// if no davenfors for email, service returns empty List
			when(submitterRep.findByEmail(any())).thenReturn(null);
			dfs = (submitterService.getAllSubmitterDavenfors(submitterEmail));
			assertTrue(dfs.size() == 0);

			verify(submitterRep, times(2)).findByEmail(any());
			verify(davenforRep, times(1)).findAllDavenforBySubmitterEmail(any());

		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

	}

	@Test
	public void addDavenforTest() {
		Davenfor dfShidduchim = getDf(catShidduchim);
		Davenfor dfRefua = getDf(catRefua);
		Davenfor dfBanim = getDf(catBanim);

		try {
			// trims names
			Davenfor readyDf = submitterService.addDavenfor(dfShidduchim, submitterEmail);
			assertEquals(readyDf.getNameEnglish().length(), 17);
			assertEquals(readyDf.getNameHebrew().length(), 12);

			// throws exception if spouse has null name
			dfBanim.setNameEnglishSpouse(null);
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				submitterService.addDavenfor(dfBanim, submitterEmail);
			});
			assertTrue(exception.getMessage().contains("spouse"));

			// other fields populated
			readyDf = submitterService.addDavenfor(dfRefua, submitterEmail);
			assertNotNull(readyDf.getCreatedAt());
			assertTrue(readyDf.getCreatedAt().isBefore(readyDf.getExpireAt()));

			verify(davenforRep, times(2)).save(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	public void updateDavenforTest() {
		try {
			// no davenfor throws exception
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				submitterService.updateDavenfor(null, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("No information"));

			// davenfor not found in repository throws exception
			Davenfor refua = getDf(catRefua);
			refua.setId(9L);
			when(davenforRep.findById(9L)).thenReturn(Optional.empty());
			exception = assertThrows(ObjectNotFoundException.class, () -> {
				submitterService.updateDavenfor(refua, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("id"));

			// other non-admin submitter can't update davenfor
			Davenfor otherDf = getDf(catRefua);
			otherDf.setId(6L);
			otherDf.setSubmitterEmail("otherEmail@gmail.com");
			when(davenforRep.findById(6L)).thenReturn(Optional.of(otherDf));
			exception = assertThrows(PermissionException.class, () -> {
				submitterService.updateDavenfor(otherDf, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("different"));

			// empty spouse info throws exception
			Davenfor missingBanimDf = getDf(catBanim);
			missingBanimDf.setNameEnglishSpouse("");
			missingBanimDf.setId(7L);
			when(davenforRep.findById(7L)).thenReturn(Optional.of(missingBanimDf));
			exception = assertThrows(EmptyInformationException.class, () -> {
				submitterService.updateDavenfor(missingBanimDf, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("spouse"));

			// works - is admin but other email
			Davenfor banimDf = getDf(catBanim);
			banimDf.setId(8L);
			when(davenforRep.findById(8L)).thenReturn(Optional.of(banimDf));
			// fix previous empty
			Davenfor updatedDf = submitterService.updateDavenfor(banimDf, "adminEmail@gmail.com", true);
			// trims names
			assertTrue(updatedDf.getNameHebrew().length() == 11);
			when(davenforRep.save(any())).thenReturn(updatedDf);
			// is updated
			assertNotNull(updatedDf.getUpdatedAt());
			// although admin submitting, submitter email is saved in davenfor
			assertTrue(submitterEmail.equals(updatedDf.getSubmitterEmail()));
			// spouse name trimmed as well
			assertTrue(updatedDf.getNameEnglishSpouse().length() == 16);

			verify(davenforRep, times(4)).findById(anyLong());
			verify(davenforRep, times(1)).save(any());

		} catch (EmptyInformationException | ObjectNotFoundException | EmailException | PermissionException
				| MessagingException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	public void extendDavenforTest() {
		try {
			// no email throws exception
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				submitterService.extendDavenfor(1, null);
			});
			assertTrue(exception.getMessage().contains("email"));

			// davenforToExtend not found throws exception
			when(davenforRep.findById(2L)).thenReturn(Optional.empty());
			exception = assertThrows(ObjectNotFoundException.class, () -> {
				submitterService.extendDavenfor(2L, submitterEmail);
			});
			assertTrue(exception.getMessage().contains("id"));

			// davenfor with diffent email logged in cannot extend
			Davenfor diffEmailDf = new Davenfor();
			diffEmailDf.setSubmitterEmail("otherEmail");
			when(davenforRep.findById(5L)).thenReturn(Optional.of(diffEmailDf));
			exception = assertThrows(PermissionException.class, () -> {
				submitterService.extendDavenfor(5L, submitterEmail);
			});

			// all okay goes through
			doNothing().when(davenforRep).extendExpiryDate(anyLong(), any(), any());
			when(davenforRep.findById(any())).thenReturn(Optional.of(getDf(catRefua)));
			boolean result = submitterService.extendDavenfor(4L, submitterEmail);
			assertTrue(result);

			verify(davenforRep, times(3)).findById(anyLong());
			verify(davenforRep, times(1)).extendExpiryDate(anyLong(), any(), any());

		} catch (ObjectNotFoundException | PermissionException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		// TODO - maybe in integration tests make test that extends davenfor with
		// different email than submitter (exception)
	}

	@Test
	public void deleteDavenforTest() {
		// davenfor not found in repository throws exception
		Davenfor refua = getDf(catRefua);
		refua.setId(9L);
		when(davenforRep.findById(9L)).thenReturn(Optional.empty());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			submitterService.deleteDavenfor(9L, submitterEmail);
		});
		assertTrue(exception.getMessage().contains("id"));

		// other non-admin submitter can't delete davenfor
		Davenfor dfToDelete = getDf(catRefua);
		when(davenforRep.findById(6L)).thenReturn(Optional.of(dfToDelete));
		exception = assertThrows(PermissionException.class, () -> {
			submitterService.deleteDavenfor(6L, "otherEmail@gmail.com");
		});
		assertTrue(exception.getMessage().contains("different"));

		// works and returns List as expected
		when(davenforRep.findById(anyLong())).thenReturn(Optional.of(getDf(catShidduchim)));
		when(davenforRep.findAllDavenforBySubmitterEmail(submitterEmail)).thenReturn(getDfList());
		List<Davenfor> returnValue = null;
		try {
			returnValue = submitterService.deleteDavenfor(1L, submitterEmail);
			assertEquals(3, returnValue.size());
		} catch (ObjectNotFoundException | PermissionException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		verify(davenforRep, times(3)).findById(anyLong());
		verify(davenforRep, times(1)).delete(any());
		verify(davenforRep, times(1)).findAllDavenforBySubmitterEmail(any());
	}

	@Test
	public void getAllCategoriesTest() {
		// no categories throws error
		when(categoryRep.findAllOrderById()).thenReturn(Collections.emptyList());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			submitterService.getAllCategories();
		});
		assertTrue(exception.getMessage().contains("categories"));

		when(categoryRep.findAllOrderById()).thenReturn(getCategoryList());
		assertDoesNotThrow(() -> {
			submitterService.getAllCategories();
		});

		verify(categoryRep, times(2)).findAllOrderById();
	}

	@Test
	public void existingOrNewSubmitterTest() {
		// once new
		when(submitterRep.findByEmail(submitterEmail)).thenReturn(null);
		assertEquals(submitterEmail, submitterService.existingOrNewSubmitter(submitterEmail));

		// once existing
		Submitter existingSub = new Submitter();
		existingSub.setEmail(submitterEmail);
		when(submitterRep.findByEmail(submitterEmail)).thenReturn(existingSub);
		assertEquals(submitterEmail, submitterService.existingOrNewSubmitter(submitterEmail));

		// save called only once
		verify(submitterRep, times(1)).save(any());

	}

	@Test
	public void getCategoryTest() {

		Category existingCategory = new Category();
		existingCategory.setCname(SOLDIERS);

		// category not found in repository, throws exception
		when(categoryRep.findById(2L)).thenReturn(Optional.empty());
		when(categoryRep.findById(3L)).thenReturn(Optional.of(existingCategory));
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			submitterService.getCategory(2L);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			Category checkCat = submitterService.getCategory(3L);
			assertEquals(SOLDIERS, checkCat.getCname());
			verify(categoryRep, times(2)).findById(anyLong());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

	}

	private Davenfor getDf(Category cat) {
		Davenfor df = new Davenfor();
		df.setSubmitterEmail(submitterEmail);
		df.setCategory(cat);

		switch (cat.getCname()) {
			case SHIDDUCHIM:
				df.setNameHebrew("שידוך בן לאה   ");
				df.setNameEnglish("Shidduch ben Leah     ");
				break;
			case BANIM:
				df.setNameHebrew("   חיים בן יפה");
				df.setNameEnglish("Chaim ben Yaffa    ");
				df.setNameHebrewSpouse("רבקה בת אסתר  ");
				df.setNameEnglishSpouse("Rivka bat Esther    ");
				break;
			case REFUA:
				df.setNameHebrew("רפואה בן מרים");
				df.setNameEnglish("Refua ben Miriam");
				break;
			default:
				return null;
		}
		return df;
	}

	private List<Davenfor> getDfList() {
		return Arrays.asList(getDf(catBanim), getDf(catShidduchim), getDf(catRefua));
	}

	private List<Category> getCategoryList() {
		return Arrays.asList(catBanim, catShidduchim, catRefua);
	}
}
