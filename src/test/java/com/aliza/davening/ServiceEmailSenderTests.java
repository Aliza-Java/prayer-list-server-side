package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUAH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.entities.User;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.UserRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.session.EmailSessionProvider;
import com.aliza.davening.util_classes.Weekly;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

//TODONOW: make a beforeEach with GreenMail start, and aftereach (or after?) with GreenMail stop.

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceEmailSenderTests {

	@MockBean
	private AuthenticationManager authenticationManager;

	@Autowired
	private EmailSessionProvider sessionProvider;

	@Autowired
	private EmailSender emailSender;

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

	public static User user1 = new User(1, null, "user1@gmail.com", "Israel", null, null, false);
	public static User user2 = new User(2, null, "user2@gmail.com", "Israel", null, null, false);
	public static User user3 = new User(3, null, "user3@gmail.com", "Israel", null, null, true);
	public static List<User> users = Arrays.asList(user1, user2, user3);

	public static Parasha parasha1 = new Parasha(1, "Bereshit", "בראשית", true);
	public static Parasha parasha2 = new Parasha(2, "Noach", "נח", false);
	public static Parasha parasha3 = new Parasha(3, "Lech Lecha", "לך-לך", false);
	public static List<Parasha> parashot = Arrays.asList(parasha1, parasha2, parasha3);

	public static Davenfor dfRefua = new Davenfor(1, "user1@gmail.com", "Refuah", "אברהם בן שרה", "Avraham ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfYeshuah1 = new Davenfor(4, "user1@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara",
			null, null, true, null, null, null, null, null);
	public static Davenfor dfBanim = new Davenfor(3, "user2@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
			"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
	public static Davenfor dfYeshuah2 = new Davenfor(4, "user2@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira",
			null, null, true, null, null, null, null, null);
	public static List<Davenfor> davenfors = Arrays.asList(dfRefua, dfYeshuah1, dfBanim, dfYeshuah2);

	private final static String UNEXPECTED_E = "   ************* Attention: @EmailSenderTest unexpected Exception: ";

	@BeforeAll
	private void baseTest() {

		System.setProperty("java.awt.headless", "true");

		when(categoryRep.findByCname(SHIDDUCHIM)).thenReturn(Optional.of(catShidduchim));
		when(categoryRep.findByCname(BANIM)).thenReturn(Optional.of(catBanim));
		when(categoryRep.findByCname(REFUA)).thenReturn(Optional.of(catRefua));
		when(categoryRep.findByCname(YESHUAH)).thenReturn(Optional.of(catYeshuah));
		when(categoryRep.findByCname(SOLDIERS)).thenReturn(Optional.of(catSoldiers));
	}

	@Test
	@Order(1)
	void sendEmailTest() throws MessagingException {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		MimeMessage message = new MimeMessage(sessionProvider.getSession());
		message.setFrom("davening44@gmail.com");
		message.addRecipient(Message.RecipientType.TO, new InternetAddress("test@gmail.com"));
		message.setSubject("Test Email");
		message.setText("This is a test email.");

		emailSender.sendEmail(message);

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
	public void sendEmailFromAdminTest() {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		Exception exception = assertThrows(EmptyInformationException.class, () -> {
			emailSender.sendEmailFromAdmin(null, "some text");
		});
		assertTrue(exception.getMessage().contains("address missing"));

		try {
			emailSender.sendEmailFromAdmin("recip@gmail.com", "test");

			// Verify that the email was sent
			greenMail.waitForIncomingEmail(5000, 2);

			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			// since BCC is purposefully stripped off the headers, can only verify that 2
			// emails were sent. Both will have the same to field and no bcc.
			// Bcc can be added as a separate header and checked in the test but I think
			// that defies the purpose
			assertEquals(2, receivedMessages.length);
			assertEquals("Message from davening list admin", receivedMessages[0].getSubject());
			assertEquals("Message from davening list admin", receivedMessages[1].getSubject());
			assertEquals("recip@gmail.com",
					(receivedMessages[0].getRecipients(MimeMessage.RecipientType.TO)[0]).toString());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("test"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[1]).contains("test"));
		} catch (EmptyInformationException | MessagingException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		greenMail.stop();
	}

	@Test
	@Order(3)
	public void sendOutWeeklyTest() {
		when(categoryRep.findById(2L)).thenReturn(Optional.empty());

		Weekly infoNoCategory = new Weekly();
		infoNoCategory.setCategoryId(2L);
		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			emailSender.sendOutWeekly(infoNoCategory);
		});
		assertTrue(exception.getMessage().contains("Category"));

		when(userRep.getAllUsersEmails())
				.thenReturn(users.stream().map(User::getEmail).collect(Collectors.toList()));

		when(categoryRep.findAll()).thenReturn(categories);

		Weekly info = new Weekly("Vayeshev", 5L, "yeshuah", "special information");

		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			when(davenforRep.findAllDavenforByCategory(YESHUAH.toString())).thenReturn(Collections.emptyList());

			exception = assertThrows(EmptyInformationException.class, () -> {
				emailSender.sendOutWeekly(info);
			});
			assertTrue(exception.getMessage().contains("no names"));

			when(davenforRep.findAllDavenforByCategory(any())).thenReturn(Arrays.asList(dfYeshuah1, dfYeshuah2));
			emailSender.sendOutWeekly(info);

			// Verify that the email was sent
			greenMail.waitForIncomingEmail(5000, 4);

			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			assertEquals(4, receivedMessages.length);
			assertTrue(receivedMessages[3].getSubject().contains("Vayeshev"));

			assertEquals("davening.list@gmail.com",
					(receivedMessages[0].getRecipients(MimeMessage.RecipientType.TO)[0]).toString());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("To unsubscribe from the weekly"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("special information"));

			MimeMessage message = receivedMessages[0];
			Object content = message.getContent();
			System.out.println("Content-Type: " + message.getContentType());

			assertTrue(message.getContentType().startsWith("multipart/"));

			boolean attachmentFound = false;

			if (content instanceof MimeMultipart) {
				MimeMultipart multipart = (MimeMultipart) content;

				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
						attachmentFound = true;
						// Read the attachment content
						String fileName = bodyPart.getFileName();
						assertNotNull(fileName);
						assertTrue(fileName.contains("Davening List Parashat"));

						try (BufferedReader reader = new BufferedReader(
								new InputStreamReader(bodyPart.getInputStream()))) {
							String contentInFile = reader.lines().collect(Collectors.joining("\n"));
							assertTrue(contentInFile.toLowerCase().contains("refua"));
							assertTrue(contentInFile.toLowerCase().contains("vayeshev"));
						}
					}
				}
				assertTrue(attachmentFound);
			}
		} catch (EmptyInformationException | MessagingException | IOException | ObjectNotFoundException e) {
			e.printStackTrace();
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		greenMail.stop();

		verify(categoryRep, times(1)).findById(any());
		verify(userRep, times(2)).getAllUsersEmails();
	}

	@Test
	@Order(4)
	public void sendOutWeeklyNoParashaTest() {

		when(userRep.getAllUsersEmails())
				.thenReturn(users.stream().map(User::getEmail).collect(Collectors.toList()));
		when(categoryRep.findAll()).thenReturn(categories);
		when(davenforRep.findAllDavenforByCategory("YESHUAH")).thenReturn(Arrays.asList(dfYeshuah1, dfYeshuah2));

		Weekly info = new Weekly(null, 5L, "yeshuah", "special information");

		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			System.out.println(davenforRep.findAll());
			emailSender.sendOutWeekly(info);

			greenMail.waitForIncomingEmail(5000, 4);

			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			assertEquals(4, receivedMessages.length);
			assertTrue(receivedMessages[3].getSubject().contains("-"));
			assertEquals(35, receivedMessages[3].getSubject().length());

			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("special information"));

			MimeMessage message = receivedMessages[0];
			Object content = message.getContent();
			assertTrue(message.getContentType().startsWith("multipart/"));

			if (content instanceof MimeMultipart) {
				MimeMultipart multipart = (MimeMultipart) content;

				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
						// Read the attachment content
						String fileName = bodyPart.getFileName();
						assertNotNull(fileName);
						assertTrue(fileName.contains("Davening List"));
						assertTrue(fileName.contains("-"));
						assertEquals(24, fileName.length());
					}
				}
			}
		} catch (EmptyInformationException | MessagingException | IOException | ObjectNotFoundException e) {
			e.printStackTrace();
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		greenMail.stop();

		verify(userRep, times(1)).getAllUsersEmails();
		verify(davenforRep, times(1)).findAllDavenforByCategory(any());

	}

	@Test
	@Order(5)
	public void sendSimplifiedWeeklyTest() {
		// can't find current parasha throws exception
		when(parashaRep.findCurrent()).thenReturn(Optional.empty());

		Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
			emailSender.sendSimplifiedWeekly();
		});
		assertTrue(exception.getMessage().toLowerCase().contains("current parasha"));

		// can't find current category throws exception
		when(parashaRep.findCurrent()).thenReturn(Optional.of(parasha2));
		when(categoryRep.getCurrent()).thenReturn(Optional.empty());

		exception = assertThrows(ObjectNotFoundException.class, () -> {
			emailSender.sendSimplifiedWeekly();
		});
		assertTrue(exception.getMessage().contains("current category"));

		when(categoryRep.getCurrent()).thenReturn(Optional.of(catRefua));
		when(userRep.getAllUsersEmails())
				.thenReturn(users.stream().map(User::getEmail).collect(Collectors.toList()));
		when(categoryRep.findAll()).thenReturn(categories);

		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			when(davenforRep.findAllDavenforByCategory(REFUA.toString())).thenReturn(Arrays.asList(dfRefua));
			when(davenforRep.findAllDavenforByCategory(YESHUAH.toString())).thenReturn(Arrays.asList(dfYeshuah1, dfYeshuah2));
			System.out.println(davenforRep.findAll());
			emailSender.sendSimplifiedWeekly();

			// Verify that the email was sent
			greenMail.waitForIncomingEmail(5000, 4);

			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			assertEquals(4, receivedMessages.length);
			assertTrue(receivedMessages[3].getSubject().contains("Noach"));

			assertEquals("davening.list@gmail.com",
					(receivedMessages[0].getRecipients(MimeMessage.RecipientType.TO)[0]).toString());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("To unsubscribe from the weekly"));

			MimeMessage message = receivedMessages[0];
			Object content = message.getContent();

			assertTrue(message.getContentType().startsWith("multipart/"));

			if (content instanceof MimeMultipart) {
				MimeMultipart multipart = (MimeMultipart) content;

				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
						String fileName = bodyPart.getFileName();
						assertNotNull(fileName);
						assertTrue(fileName.contains("Davening List Parashat"));

						try (BufferedReader reader = new BufferedReader(
								new InputStreamReader(bodyPart.getInputStream()))) {
							String contentInFile = reader.lines().collect(Collectors.joining("\n"));
							assertTrue(contentInFile.toLowerCase().contains("refua"));
							assertTrue(contentInFile.toLowerCase().contains("noach"));
						}
					}
				}
			}
		} catch (EmptyInformationException | MessagingException | IOException | ObjectNotFoundException e) {
			e.printStackTrace();
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		greenMail.stop();

		verify(parashaRep, times(3)).findCurrent();
		verify(categoryRep, times(2)).getCurrent();
		verify(userRep, times(1)).getAllUsersEmails();
	}

	@Test
	@Order(6)
	public void sendUrgentEmailTest() {
		Exception exception = assertThrows(EmptyInformationException.class, () -> {
			emailSender.sendUrgentEmail(null);
		});
		assertTrue(exception.getMessage().contains("incomplete"));

		when(userRep.getAllUsersEmails())
				.thenReturn(users.stream().map(User::getEmail).collect(Collectors.toList()));

		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			dfRefua.setNote("testNote");
			emailSender.sendUrgentEmail(dfRefua);

			greenMail.waitForIncomingEmail(5000, 4);
			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			assertEquals(4, receivedMessages.length);
			assertEquals("Please daven for Avraham ben Sara", receivedMessages[0].getSubject());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("Please daven now for <b>Avraham ben Sara"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("testNote"));
			assertEquals("Please daven for Avraham ben Sara", receivedMessages[2].getSubject());
			assertTrue(GreenMailUtil.getBody(receivedMessages[2]).contains("Please daven now for <b>Avraham ben Sara"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[2]).contains("testNote"));
			greenMail.stop();

			// need to stop and start, otherwise receivedMessages adds on also previous
			greenMail.start();
			// sending Banim email
			emailSender.sendUrgentEmail(dfBanim);
			greenMail.waitForIncomingEmail(5000, 4);
			receivedMessages = greenMail.getReceivedMessages();

			assertEquals(4, receivedMessages.length);
			assertEquals("Please daven for Avraham ben Sara", receivedMessages[0].getSubject());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("Please daven now for <b>Avraham ben Sara"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("Yehudit bat Miriam"));
		} catch (EmptyInformationException | MessagingException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
		greenMail.stop();

		verify(userRep, times(2)).getAllUsersEmails();
	}

	@Test
	@Order(7)
	public void informAdminTest() {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			emailSender.informAdmin("testSubject", "testMessage");

			greenMail.waitForIncomingEmail(5000, 1);
			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			assertEquals(1, receivedMessages.length);
			assertEquals("testSubject", receivedMessages[0].getSubject());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("testMessage"));
			// that there's more than just the message sent in
			assertTrue((GreenMailUtil.getBody(receivedMessages[0]).length()) > "testMessage".length());

			greenMail.stop();
		} catch (MessagingException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(8)
	public void notifyDisactivatedUserTest() {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				emailSender.notifyDisactivatedUser(null);
			});
			assertTrue(exception.getMessage().contains("email address missing"));

			emailSender.notifyDisactivatedUser("test@tmail.com");

			greenMail.waitForIncomingEmail(5000, 2);
			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			assertEquals(2, receivedMessages.length);
			assertEquals("Message from davening list admin", receivedMessages[0].getSubject());
			assertEquals("Message from davening list admin", receivedMessages[1].getSubject());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("You will no longer receive"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[1]).contains("You will no longer receive"));

			greenMail.stop();
		} catch (MessagingException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(9)
	public void notifyActivatedUserTest() {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			Exception exception = assertThrows(EmptyInformationException.class, () -> {
				emailSender.notifyActivatedUser("");
			});
			assertTrue(exception.getMessage().contains("email address missing"));

			emailSender.notifyActivatedUser("test@tmail.com");

			greenMail.waitForIncomingEmail(5000, 2);
			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			// one for recipient, one for admin as Bcc
			assertEquals(2, receivedMessages.length);
			assertEquals("Message from davening list admin", receivedMessages[0].getSubject());
			assertEquals("Message from davening list admin", receivedMessages[1].getSubject());
			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("now be receiving emails"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[1]).contains("now be receiving emails"));

			greenMail.stop();
		} catch (MessagingException | EmptyInformationException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}

	@Test
	@Order(10)
	public void offerExtensionOrDelete() {
		GreenMail greenMail = new GreenMail(ServerSetup.SMTP);
		greenMail.start();

		try {
			emailSender.offerExtensionOrDelete(dfYeshuah1);

			greenMail.waitForIncomingEmail(5000, 2);
			MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

			// one for recipient, one for admin as Bcc
			assertEquals(2, receivedMessages.length);
			assertEquals("user1@gmail.com",
					(receivedMessages[0].getRecipients(MimeMessage.RecipientType.TO)[0]).toString());
			assertEquals("Davening List Confirmation", receivedMessages[0].getSubject());
			assertEquals("Davening List Confirmation", receivedMessages[1].getSubject());
			System.out.println(GreenMailUtil.getBody(receivedMessages[0]).toString());

			assertTrue(GreenMailUtil.getBody(receivedMessages[0]).contains("the name on the list"));
			assertTrue(GreenMailUtil.getBody(receivedMessages[1]).contains("Remove this name"));

			greenMail.stop();
		} catch (MessagingException e) {
			System.out.println(UNEXPECTED_E + e.getStackTrace());
		}
	}
}