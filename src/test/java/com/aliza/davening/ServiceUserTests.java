package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUA_AND_PARNASSA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.User;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.UserRepository;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.UserService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceUserTests {

	@MockBean
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserService userService;

	@MockBean
	private DavenforRepository davenforRep;

	@MockBean
	private CategoryRepository categoryRep;

	@MockBean
	private UserRepository userRep;

	@MockBean
	private AdminRepository adminRep;

	@MockBean
	private EmailSender emailSender;

	@MockBean
	private JwtUtils jwtUtils;

	static String submitterEmail = "sub.email@gmail.com";

	public static Category catRefua = new Category(REFUA, true, 180, 1);
	public static Category catShidduchim = new Category(SHIDDUCHIM, false, 40, 2);
	public static Category catBanim = new Category(BANIM, false, 50, 3);
	public static Category catSoldiers = new Category(SOLDIERS, false, 180, 4);
	public static Category catYeshua = new Category(YESHUA_AND_PARNASSA, false, 180, 5);

	private final static String UNEXPECTED_E = "   ************* Attention: @Submitter service test unexpected Exception: ";

	@BeforeAll
	private void baseTest() {
		when(userRep.findByEmail(submitterEmail)).thenReturn(Optional.of(new User(submitterEmail)));

		when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(Optional.of(catShidduchim));
		when(categoryRep.findByCname(BANIM)).thenReturn(Optional.of(catBanim));
		when(categoryRep.findByCname(REFUA)).thenReturn(Optional.of(catRefua));
		when(categoryRep.findByCname(YESHUA_AND_PARNASSA)).thenReturn(Optional.of(catYeshua));
		when(categoryRep.findByCname(SOLDIERS)).thenReturn(Optional.of(catSoldiers));
	}

	@Test
	@Order(1)
	public void getAllSubmitterDavenforsTest() throws ObjectNotFoundException {
		// service returns davenfors fetched from repository
		when(davenforRep.findAllDavenforByUserEmail(submitterEmail)).thenReturn(getDfList());
		List<Davenfor> dfs = (userService.getAllUserDavenfors(submitterEmail));
		assertTrue(dfs.size() == 3);

		// if no davenfors for email, service returns empty List
		when(userRep.findByEmail(any())).thenReturn(Optional.of(new User(submitterEmail)));
		when(davenforRep.findAllDavenforByUserEmail(any())).thenReturn(new ArrayList<Davenfor>());
		
		dfs = (userService.getAllUserDavenfors(submitterEmail));
		assertTrue(dfs.size() == 0);

		verify(userRep, times(2)).findByEmail(any());
		verify(davenforRep, times(2)).findAllDavenforByUserEmail(any());

	}

	@Test
	@Order(2)
	public void addDavenforTest() {
		Davenfor dfShidduchim = getDf(catShidduchim);
		Davenfor dfRefua = getDf(catRefua);
		Davenfor dfBanim = getDf(catBanim);
		doNothing().when(emailSender).informAdmin(any(), any());

		try {
			// trims names
			boolean response = userService.addDavenfor(dfShidduchim, submitterEmail);
			// assertEquals(readyDf.getNameEnglish().length(), 17); //TODO* - fix test to
			// see in DB instead of returned one. maybe fetch by last submitted = biggest id
			// assertEquals(readyDf.getNameHebrew().length(), 12);

			// throws exception if spouse has null name

			dfBanim.setNameEnglishSpouse(null);
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				userService.addDavenfor(dfBanim, submitterEmail);
			});
			assertTrue(exception.getMessage().contains("spouse"));

			// other fields populated
			response = userService.addDavenfor(dfRefua, submitterEmail);
			// assertNotNull(readyDf.getCreatedAt());
			// assertTrue(readyDf.getCreatedAt().isBefore(readyDf.getExpireAt()));

			verify(davenforRep, times(2)).save(any());

		} catch (Exception e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(3)
	public void updateDavenforTest() {
		try {
			// no davenfor throws exception
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				userService.updateDavenfor(null, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("No information"));

			// davenfor not found in repository throws exception
			Davenfor refua = getDf(catRefua);
			refua.setId(9L);
			when(davenforRep.findById(9L)).thenReturn(Optional.empty());
			exception = assertThrows(ObjectNotFoundException.class, () -> {
				userService.updateDavenfor(refua, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("id"));

			// other non-admin submitter can't update davenfor
			Davenfor otherDf = getDf(catRefua);
			otherDf.setId(6L);
			otherDf.setUserEmail("otherEmail@gmail.com");
			when(davenforRep.findById(6L)).thenReturn(Optional.of(otherDf));
			exception = assertThrows(PermissionException.class, () -> {
				userService.updateDavenfor(otherDf, submitterEmail, false);
			});
			assertTrue(exception.getMessage().contains("different"));

			// No spouse info throws exception
			Davenfor missingBanimDf = getDf(catBanim);
			missingBanimDf.setNameEnglishSpouse("");
			missingBanimDf.setNameHebrewSpouse("");
			missingBanimDf.setId(7L);
			when(davenforRep.findById(7L)).thenReturn(Optional.of(missingBanimDf));
			exception = assertThrows(EmptyInformationException.class, () -> {
				userService.updateDavenfor(missingBanimDf, submitterEmail, false);
			});
			assert (exception.getMessage().contains("spouse"));

			// works - is admin but other email
			Davenfor banimDf = getDf(catBanim);
			banimDf.setId(8L);
			when(davenforRep.findById(8L)).thenReturn(Optional.of(banimDf));
			// fix previous empty
			Davenfor updatedDf = userService.updateDavenfor(banimDf, "adminEmail@gmail.com", true);
			// trims names
			assertTrue(updatedDf.getNameHebrew().length() == 11);
			when(davenforRep.save(any())).thenReturn(updatedDf);
			// is updated
			assertNotNull(updatedDf.getUpdatedAt());
			// although admin submitting, submitter email is saved in davenfor
			assertTrue(submitterEmail.equals(updatedDf.getUserEmail()));
			// spouse name trimmed as well
			assertTrue(updatedDf.getNameEnglishSpouse().length() == 16);

			verify(davenforRep, times(4)).findById(anyLong());
			verify(davenforRep, times(1)).save(any());

		} catch (EmptyInformationException | ObjectNotFoundException | PermissionException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(4)
	public void extendDavenforTest() {

		try {
			// no email throws exception
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				userService.extendDavenfor(1, null);
			});
			assertTrue(exception.getMessage().contains("email"));

			// davenforToExtend not found throws exception
			when(davenforRep.findById(2L)).thenReturn(Optional.empty());
			when(jwtUtils.extractEmailFromToken(any())).thenReturn(submitterEmail);
			exception = assertThrows(ObjectNotFoundException.class, () -> {
				userService.extendDavenfor(2L, submitterEmail);
			});
			assertTrue(exception.getMessage().contains("id"));

			// davenfor with diffent email logged in cannot extend
			Davenfor diffEmailDf = new Davenfor();
			diffEmailDf.setUserEmail("otherEmail");
			when(davenforRep.findById(5L)).thenReturn(Optional.of(diffEmailDf));
			exception = assertThrows(PermissionException.class, () -> {
				userService.extendDavenfor(5L, submitterEmail);
			});

			// all okay goes through
			// doNothing().when(davenforRep).extendExpiryDate(anyLong(), any(), any());
			when(davenforRep.findById(any())).thenReturn(Optional.of(getDf(catRefua)));
			Davenfor result = userService.extendDavenfor(4L, submitterEmail);
			// assertTrue(result); TODO*: fix. what need to assert?

			verify(davenforRep, times(3)).findById(anyLong());
			// verify(davenforRep, times(1)).extendExpiryDate(anyLong(), any(), any());

		} catch (ObjectNotFoundException | PermissionException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		// TODO* - maybe in integration tests make test that extends davenfor with
		// different email than submitter (exception)
	}

	@Test
	@Order(5)
	public void deleteDavenforTest() {

		// davenfor not found in repository throws exception
		Davenfor refua = getDf(catRefua);
		refua.setId(9L);
		when(davenforRep.findById(9L)).thenReturn(Optional.empty());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			userService.deleteDavenfor(9L, submitterEmail, false);
		});
		assertTrue(exception.getMessage().contains("id"));

		// other non-admin submitter can't delete davenfor
		Davenfor dfToDelete = getDf(catRefua);
		when(davenforRep.findByIdIncludingDeleted(6L)).thenReturn(Optional.of(dfToDelete));
		exception = assertThrows(PermissionException.class, () -> {
			userService.deleteDavenfor(6L, "otherEmail@gmail.com", false);
		});
		assertTrue(exception.getMessage().contains("different"));

		// works and returns List as expected
		when(davenforRep.findByIdIncludingDeleted(anyLong())).thenReturn(Optional.of(getDf(catShidduchim)));
		when(davenforRep.findAllDavenforByUserEmail(submitterEmail)).thenReturn(getDfList());
		List<Davenfor> returnValue = null;
		try {
			returnValue = userService.deleteDavenfor(1L, submitterEmail, false);
			assertEquals(3, returnValue.size());
		} catch (ObjectNotFoundException | PermissionException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		verify(davenforRep, times(3)).findByIdIncludingDeleted(anyLong());
		verify(davenforRep, times(1)).softDeleteById(any());
		verify(davenforRep, times(1)).findAllDavenforByUserEmail(any());
	}

	@Test
	@Order(6)
	public void getAllCategoriesTest() {

		// no categories throws error
		when(categoryRep.findAllOrderById()).thenReturn(Collections.emptyList());
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			userService.getAllCategories();
		});
		assertTrue(exception.getMessage().contains("categories"));

		when(categoryRep.findAllOrderById()).thenReturn(getCategoryList());
		assertDoesNotThrow(() -> {
			userService.getAllCategories();
		});

		verify(categoryRep, times(2)).findAllOrderById();
	}

	@Test
	@Order(7)
	public void existingOrNewSubmitterTest() {
		// once new
		when(userRep.findByEmail(submitterEmail)).thenReturn(Optional.empty());
		assertEquals(submitterEmail, userService.existingOrNewUser(submitterEmail));

		// once existing
		User existingSub = new User();
		existingSub.setEmail(submitterEmail);
		when(userRep.findByEmail(submitterEmail)).thenReturn(Optional.of(existingSub));
		assertEquals(submitterEmail, userService.existingOrNewUser(submitterEmail));

		// save called only once
		verify(userRep, times(1)).save(any());

	}

	@Test
	@Order(8)
	public void getCategoryTest() {
		Category existingCategory = new Category();
		existingCategory.setCname(SOLDIERS);

		// category not found in repository, throws exception
		when(categoryRep.findById(2L)).thenReturn(Optional.empty());
		when(categoryRep.findById(3L)).thenReturn(Optional.of(existingCategory));
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			userService.getCategory(2L);
		});
		assertTrue(exception.getMessage().contains("id"));

		try {
			Category checkCat = userService.getCategory(3L);
			assertEquals(SOLDIERS, checkCat.getCname());
			verify(categoryRep, times(2)).findById(anyLong());
		} catch (ObjectNotFoundException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}

	}

	private Davenfor getDf(Category cat) {
		Davenfor df = new Davenfor();
		df.setUserEmail(submitterEmail);
		df.setCategory(cat.getCname().toString());

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
