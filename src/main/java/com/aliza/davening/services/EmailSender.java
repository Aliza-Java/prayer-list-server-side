package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailScheme;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.repositories.UserRepository;
import com.aliza.davening.security.JwtUtils;
import com.aliza.davening.services.session.EmailSessionProvider;
import com.aliza.davening.util_classes.Weekly;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service
public class EmailSender {

	@Autowired
	private EmailSessionProvider sessionProvider;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ParashaRepository parashaRepository;

	@Autowired
	private DavenforRepository davenforRepository;

	@Autowired
	private Utilities utilities;

	@Autowired
	private JwtUtils jwtUtils;

	@Value("${admin.email}")
	String adminEmail;

	public final String client = SchemeValues.client;

	// @Value("${admin.id}")
	long adminId = 1;

	@Value("${link.to.confirm}")
	String linkToConfirmPartial;

	@Value("${link.to.remove}")
	String linkToRemoveClient;

	@Value("${link.to.unsubscribe}")
	String linkToUnsubscribe;

	public MimeMessage createMimeMessage(Session session, String subject, String text, String to, List<String> bccList,
			File attachment, String attachmentName) {

		// Create the email message
		MimeMessage message = new MimeMessage(session);
		try {
			message.setRecipients(Message.RecipientType.TO, to);
			if (bccList != null) {
				bccList.forEach(bcc -> {
					try {
						message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
					} catch (MessagingException e) {
						System.out.println("Failed to send to " + bcc + " as bcc");
					}
				});
			}
			message.setSubject(subject);

			Multipart multipart = new MimeMultipart();

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(text, "text/html; charset=UTF-8");
			multipart.addBodyPart(textPart);

			if (attachment != null) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				attachmentPart.attachFile(attachment);
				attachmentPart.setFileName(attachmentName);
				multipart.addBodyPart(attachmentPart);
			}

			message.setContent(multipart);

		} catch (MessagingException | IOException e) {
			throw new RuntimeException("Failed to create MIME message", e);
		}

		return message;
	}

	// general method
	// tested
	public boolean sendEmail(MimeMessage message) {
		try {
			Transport.send(message);

			Address[] toRecipients = message.getRecipients(Message.RecipientType.TO);
			// int toAmount = (toRecipients != null) ? toRecipients.length : 0;

			Address[] ccRecipients = message.getRecipients(Message.RecipientType.CC);
			// int ccAmount = (ccRecipients != null) ? ccRecipients.length : 0;

			Address[] bccRecipients = message.getRecipients(Message.RecipientType.BCC);
			// int bccAmount = (bccRecipients != null) ? bccRecipients.length : 0;

			Address[] emptyArray = new Address[0];

			List<Address> allRecipients = Stream
					.of((toRecipients == null ? emptyArray : toRecipients),
							ccRecipients == null ? emptyArray : ccRecipients,
							bccRecipients == null ? emptyArray : bccRecipients)
					.flatMap(Arrays::stream).distinct().collect(Collectors.toList());

			String additionalMessage = "";
			if (allRecipients.size() > 1)
				additionalMessage = String.format("(and %d more) ", allRecipients.size() - 1);

			System.out.println(String.format("Email to %s %ssent successfully!", allRecipients.get(0).toString(),
					additionalMessage));

		} catch (MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to send email");
		}

		return true;
	}

	// A general method allowing Admin to send messages to system users public
	// tested
	public void sendEmailFromAdmin(String recipient, String text) throws EmptyInformationException {

		if (recipient == null || recipient.length() == 0) {
			throw new EmptyInformationException("Recipient email address missing.");
		}

		Session session = sessionProvider.getSession();
		MimeMessage mimeMessage = createMimeMessage(session, EmailScheme.adminMessageSubject, text, recipient, null,
				null, null);

		sendEmail(mimeMessage);
	}

	// tested
	public void sendSimplifiedWeekly() throws Exception {
		Weekly simplified = new Weekly();

		Parasha parasha = parashaRepository.findCurrent()
				.orElseThrow(() -> new ObjectNotFoundException("current Parasha"));
		simplified.parashaName = parasha.getEnglishName();

		Category category = categoryRepository.getCurrent()
				.orElseThrow(() -> new ObjectNotFoundException("current category"));
		simplified.cName = category.getCname().toString();

		sendOutWeekly(simplified);
	}

	// tested
	public void sendOutWeekly(Weekly info) throws Exception {

		Category category;
		if (info.cName != null && info.cName.length() > 0)
			category = Category.getCategory(info.cName);
		else
			category = categoryRepository.findById(info.categoryId)
					.orElseThrow(() -> new ObjectNotFoundException("Category"));

		LocalDate date = LocalDate.now();
		String todaysDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(date);
		String parashaName = (info.parashaName != null && info.parashaName.length() > 0)
				? "Parashat " + info.parashaName + " - " + todaysDate
				: todaysDate;
		String subject = String.format(EmailScheme.weeklyEmailSubject, parashaName);
		String linkToUnsubscribe = client + "/unsubscribe";
		String emailText = String.format(EmailScheme.weeklyEmailText, linkToUnsubscribe);

		// If there is a message from the admin, add it beforehand.
		if (info.message != null) {
			emailText = concatAdminMessage(info.message, emailText);
		}

		List<String> usersList = userRepository.getAllUsersEmails();

		String fileName = String.format(EmailScheme.weeklyFileName, utilities.getFileName(parashaName));

		// 'to' field in doEmail cannot be empty (JavaMailSender in subsequent methods),
		// therefore including admin's email.

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText, null, usersList,
				utilities.buildListImage(category, info.parashaName), fileName));
	}

	// tested
	public void sendUrgentEmail(Davenfor davenfor) throws EmptyInformationException {

		if (davenfor == null || davenfor.getNameEnglish().isEmpty()) { // TODO*: maybe put a validity check here
			throw new EmptyInformationException("The name you submitted for davening is incomplete.  ");
		}

		List<String> davenersList = userRepository.getAllUsersEmails();

		// Specifying the name in order to avoid Gmail from bunching up many urgent
		// names under a single email thread.
		String subject = String.format("Please daven for %s", davenfor.getNameEnglish());

		String urgentMessage;

		// If category is banim, need to list also spouse name (if exists in at least
		// one language).
		if (Category.isBanim(davenfor.getCategory())
				&& (davenfor.getNameEnglishSpouse() != null || davenfor.getNameHebrewSpouse() != null)) {
			urgentMessage = String.format(EmailScheme.urgentDavenforEmailBanim, davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getNameEnglishSpouse(), davenfor.getNameHebrewSpouse(),
					SchemeValues.banimName);
		}

		else {
			urgentMessage = String.format(EmailScheme.urgentDavenforEmailText, davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getCategory());
		}

		if (davenfor.getNote() != null) {
			urgentMessage = concatAdminMessageAfter(davenfor.getNote(), urgentMessage);
		}

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, urgentMessage, null, davenersList, null,
				null));
	}

	// tested
	public void informAdmin(String subject, String message) {
		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, adminEmail, null, null, null));
	}

	public boolean sendConfirmationEmail(long davenforId) throws EmailException, IOException, ObjectNotFoundException {

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor confirmedDavenfor = optionalDavenfor.get();
		String subject = EmailScheme.confirmationEmailSubject;
		String emailAddress = confirmedDavenfor.getUserEmail();

		String linkToConfirm = String.format(linkToConfirmPartial, davenforId, emailAddress);
		String linkToRemove = String.format(client + linkToRemoveClient, davenforId, jwtUtils.generateEmailToken(emailAddress));

		String emailText = new String(Files.readAllBytes(Paths.get(EmailScheme.confirmationEmailTextLocation)),
				StandardCharsets.UTF_8);
		String personalizedEmailText = String.format(emailText, confirmedDavenfor.getNameEnglish(),
				confirmedDavenfor.getCategory(), linkToConfirm, linkToRemove);
		String to = confirmedDavenfor.getUserEmail();

		try {
			sendEmail(createMimeMessage(sessionProvider.getSession(), subject, personalizedEmailText, to, null, null,
					null));
		} catch (Exception e) {
			throw new EmailException(String.format("Could not send confirmation email to %s", emailAddress));
		}

		return true;
	}

	// tested
	public void notifyDisactivatedUser(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.userDisactivated);
	}

	// tested
	public void notifyActivatedUser(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.userActivated);
	}

	// tested
	public void offerExtensionOrDelete(Davenfor davenfor) {

		String subject = EmailScheme.expiringNameSubject;
		String message = String.format(utilities.setExpiringNameMessage(davenfor));
		String recipient = davenfor.getUserEmail();

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, recipient, null, null, null));
	}

	public String requestToUnsubscribe(String email) {// TODO*: test
		MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), EmailScheme.unsubscribeSubject,
				setUnsubscribeMessage(email), email, null, null, null);
		sendEmail(mimeMessage);
		return String.format("Please check your email address: %s for an 'Unsubscribe' message", email);
	}

	private String concatAdminMessage(String adminMessage, String emailText) {
		// adding admin message before name and bolding it according to settings in
		// EmailScheme.
		return String.format(EmailScheme.boldFirstMessage, adminMessage, emailText);
	}

	private String concatAdminMessageAfter(String adminMessage, String emailText) {
		// adding admin message after name and bolding it according to settings in
		// EmailScheme.
		return String.format(EmailScheme.boldSecondMessage, emailText, adminMessage);
	}

	private String setUnsubscribeMessage(String email) {
		String unsubscribeLink = linkToUnsubscribe + jwtUtils.generateEmailToken(email);
		return String.format(EmailScheme.unsubscribeMessage, unsubscribeLink, adminEmail);
	}
}
