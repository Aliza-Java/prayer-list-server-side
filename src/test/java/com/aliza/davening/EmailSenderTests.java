package com.aliza.davening;

import static com.aliza.davening.entities.CategoryType.BANIM;
import static com.aliza.davening.entities.CategoryType.REFUA;
import static com.aliza.davening.entities.CategoryType.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryType.SOLDIERS;
import static com.aliza.davening.entities.CategoryType.YESHUAH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.repositories.SubmitterRepository;
import com.aliza.davening.security.LoginRequest;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.TestEmailSessionProvider;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailSenderTests {

	@Autowired
	private AdminService adminService;
	
	@Autowired
	private EmailSender emailSender;
	
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

	private final static String UNEXPECTED_E = "   ************* Attention: @EmailSenderTest unexpected Exception: ";

	@BeforeAll
	private void baseTest() {


		when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(catShidduchim);
		when(categoryRep.findByCname(BANIM)).thenReturn(catBanim);
		when(categoryRep.findByCname(REFUA)).thenReturn(catRefua);
		when(categoryRep.findByCname(YESHUAH)).thenReturn(catYeshuah);
		when(categoryRep.findByCname(SOLDIERS)).thenReturn(catSoldiers);
	}

	@Test
	@Order(1)
	void sendEmailTest() throws MessagingException {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
        greenMail.start();

        MimeMessage message = new MimeMessage(getSessionForTest(greenMail));
        message.setFrom("davening44@gmail.com");
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("test@gmail.com"));
        message.setSubject("Test Email");
        message.setText("This is a test email.");

        try {
			emailSender.sendEmail(message);
		} catch (EmailException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
      
		// Verify that the email was sent 
        greenMail.waitForIncomingEmail(5000, 1);

		MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		assertEquals(1, receivedMessages.length);
		assertEquals("Test Email", receivedMessages[0].getSubject());
		assertEquals("This is a test email.", GreenMailUtil.getBody(receivedMessages[0]));

		greenMail.stop();
	}

	@Test
	@Order(2)
	public void sendEmailFromAdminTest()
	{
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
        greenMail.start();
        
		Exception exception = assertThrows(EmptyInformationException.class, () -> {
			emailSender.sendEmailFromAdmin(null, "some text");
		});
		assertTrue(exception.getMessage().contains("address missing"));
		
		
	}
	
	@Test
	@Order(10)
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
	
	
	private Session getSessionForTest(GreenMail greenMail)
	{
		// Set up session for GreenMail
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "localhost");
		properties.put("mail.smtp.port", greenMail.getSmtp().getPort());
		return Session.getInstance(properties);
	}
    
}
