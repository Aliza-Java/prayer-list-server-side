package com.aliza.davening;

import static com.aliza.davening.entities.CategoryType.BANIM;
import static com.aliza.davening.entities.CategoryType.REFUA;
import static com.aliza.davening.entities.CategoryType.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryType.SOLDIERS;
import static com.aliza.davening.entities.CategoryType.YESHUAH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.SubmitterService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
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

	@Test
	public void addDavenfor() {
		// Mock repository response
		Mockito.when(submitterRep.findByEmail(submitterEmail)).thenReturn(new Submitter(submitterEmail));
		try {
			Mockito.when(emailSender.sendEmail(Mockito.anyString(), // subject
					Mockito.anyString(), // text
					Mockito.anyString(), // to,
					Mockito.any(), // bcc,
					Mockito.any(), // attachment,
					Mockito.anyString())) // attachmentName
					.thenReturn(true);
		} catch (MessagingException | EmailException e) {
			// do nothing
		}

		Category catRefua = new Category(REFUA, true, 180, 1);
		Category catShidduchim = new Category(SHIDDUCHIM, false, 40, 2);
		Category catBanim = new Category(BANIM, false, 50, 3);
		Category catSoldiers = new Category(SOLDIERS, false, 180, 4);
		Category catYeshuah = new Category(YESHUAH, false, 180, 5);

		Mockito.when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(catShidduchim);
		Mockito.when(categoryRep.findByCname(BANIM)).thenReturn(catBanim);
		Mockito.when(categoryRep.findByCname(REFUA)).thenReturn(catRefua);
		Mockito.when(categoryRep.findByCname(YESHUAH)).thenReturn(catYeshuah);
		Mockito.when(categoryRep.findByCname(SOLDIERS)).thenReturn(catSoldiers);

		Davenfor dfShidduchim = getDf(catShidduchim);
		Davenfor dfRefua = getDf(catRefua);
		Davenfor dfBanim = getDf(catBanim);

		try {
			Davenfor readyDf = submitterService.addDavenfor(dfShidduchim, submitterEmail);
			// trims names
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

		} catch (Exception e) {
			System.out.println("@Test addDavenFor failed");
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
				df.setNameHebrew("חיים בן יפה");
				df.setNameEnglish("Chaim ben Yaffa");
				df.setNameHebrewSpouse("רבקה בת אסתר");
				df.setNameEnglishSpouse("Rivka bat Esther");
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
}
