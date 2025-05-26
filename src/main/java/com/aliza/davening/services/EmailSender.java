package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
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
	private DavenforRepository davenforRepository;

	@Autowired
	private AdminService adminService;

	@Autowired
	private Utilities utilities;

	@Autowired
	private JwtUtils jwtUtils;

	@Value("${admin.email}")
	String adminEmail;

	public final String client = SchemeValues.client;

	// @Value("${admin.id}")
	long adminId = 1;

	@Value("${link.to.extend}")
	String linkToExtendClient;

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
	public void sendEmailFromAdmin(String recipient, String text, String suggestedSubject)
			throws EmptyInformationException {

		if (recipient == null || recipient.length() == 0) {
			throw new EmptyInformationException("Recipient email address missing.");
		}

		Session session = sessionProvider.getSession();

		String subject = (suggestedSubject == null || suggestedSubject.trim().length() == 0)
				? EmailScheme.adminMessageSubject
				: suggestedSubject;

		MimeMessage mimeMessage = createMimeMessage(session, subject, text, recipient, null, null, null);

		sendEmail(mimeMessage);
	}

	// tested
	public boolean sendOutWeekly(Weekly info)
			throws EmailException, EmptyInformationException, ObjectNotFoundException {

		Category category;
		String parashaNameEnglish;
		String parashaNameFull;
		String linkToUnsubscribe = client + "/unsubscribe";
		String emailText = String.format(EmailScheme.weeklyEmailText, linkToUnsubscribe);

		if (info == null) {
			category = adminService.inferCategory();
			parashaNameEnglish = adminService.inferParashaName(false);
			parashaNameFull = adminService.inferParashaName(true);
		} else // info came in (but parts may be missing)
		{
			if (info.category != null && info.category.length() > 0)
				category = Category.getCategory(info.category);
			else
				category = categoryRepository.findById(info.categoryId)
						.orElseThrow(() -> new ObjectNotFoundException("Category"));

			parashaNameEnglish = (info.parashaNameEnglish != null && info.parashaNameEnglish.length() > 0)
					? info.parashaNameEnglish
					: adminService.inferParashaName(false);

			parashaNameFull = info.parashaNameFull;

			if (info.message != null)
				emailText = concatAdminMessage(info.message, emailText);
		}

		String subject = String.format(EmailScheme.weeklyEmailSubject, parashaNameEnglish);

		List<String> usersList = userRepository.getAllUsersEmails();
		if (usersList.size() == 0)
			throw new EmailException("There are no active users, cannot send list");

		String fileName = String.format(EmailScheme.weeklyFileName, utilities.formatFileName(parashaNameEnglish));

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText, null, usersList,
				utilities.buildListImage(category, parashaNameFull, fileName), fileName));

		return true;
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
					davenfor.getNameHebrew(), utilities.toTitlecase(davenfor.getCategory()));
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

		// todo* in future - make these a method (used twice)

		String emailText = new String(Files.readAllBytes(Paths.get(EmailScheme.confirmationEmailTextLocation)),
				StandardCharsets.UTF_8);
		String personalizedEmailText = String.format(emailText, confirmedDavenfor.getNameEnglish(),
				Category.getCategory(confirmedDavenfor.getCategory()).getCname().getVisual(), getLinkToExtend(confirmedDavenfor),
				getLinkToDelete(confirmedDavenfor));

		try {
			sendEmail(createMimeMessage(sessionProvider.getSession(), subject, personalizedEmailText, emailAddress,
					null, null, null));
		} catch (Exception e) {
			throw new EmailException(String.format("Could not send confirmation email to %s", emailAddress));
		}

		return true;
	}

	// tested
	public void offerExtensionOrDelete(Davenfor davenfor) {

		String subject = EmailScheme.expiringNameSubject;
		String message = String.format(utilities.setExpiringNameMessage(davenfor));
		String recipient = davenfor.getUserEmail();

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, recipient, null, null, null));
	}

	// tested
	public void notifyDisactivatedUser(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.userDisactivated, EmailScheme.userDisactivatedSubject);
	}

	// tested
	public void notifyActivatedUser(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.userActivated, EmailScheme.userActivatedSubject);
	}

	public String requestToUnsubscribe(String email) {// TODO*: test
		MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), EmailScheme.unsubscribeSubject,
				setUnsubscribeMessage(email), email, null, null, null);
		sendEmail(mimeMessage);
		return String.format(
				"We sent you a link to complete the process. Please check your email address: %s for an 'Unsubscribe' message",
				email);
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
		long halfHour = 1000 * 60 * 30;
		Date expiration = new Date(new Date().getTime() + halfHour);

		String unsubscribeLink = linkToUnsubscribe + jwtUtils.generateEmailToken(email, expiration);
		return String.format(EmailScheme.unsubscribeMessage, unsubscribeLink, adminEmail);
	}

	public String getLinkToExtend(Davenfor davenfor) {
		long week = utilities.getDaysInMs(7);
		Date expiration = new Date(new Date().getTime() + week);
		return String.format(client + linkToExtendClient, davenfor.getId(),
				URLEncoder.encode(davenfor.getNameEnglish(), StandardCharsets.UTF_8),
				jwtUtils.generateEmailToken(davenfor.getUserEmail(), expiration));
	}

	public String getLinkToDelete(Davenfor davenfor) {
		long week = utilities.getDaysInMs(7);
		Date expiration = new Date(new Date().getTime() + week);
		return String.format(client + linkToRemoveClient, davenfor.getId(),
				jwtUtils.generateEmailToken(davenfor.getUserEmail(), expiration));
	}
}
