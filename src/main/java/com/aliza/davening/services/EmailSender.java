package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
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
	public final String server = SchemeValues.server;

	// @Value("${admin.id}")
	long adminId = 1;

	@Value("${link.to.extend}")
	String linkToExtendServer;

	@Value("${link.to.remove}")
	String linkToRemoveServer;

	@Value("${link.to.unsubscribe}")
	String linkToUnsubscribe;

	public MimeMessage createMimeMessage(Session session, String subject, String text, String to, List<String> bccList,
			File attachment, String attachmentName) {

		// Create the email message
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress("davening.list@gmail.com", "Emek Hafrashat Challah Davening List")); //*TODO in future - put this in env file
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
			String formattedText = String.format(EmailScheme.emailBodyStyle, text);
			textPart.setContent(formattedText, "text/html; charset=UTF-8");
			multipart.addBodyPart(textPart); // email body

			if (attachment != null) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				attachmentPart.attachFile(attachment);
				attachmentPart.setFileName(attachmentName);
				multipart.addBodyPart(attachmentPart); // the attachment
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

			System.out.println(String.format("Email to %s %s sent successfully!", allRecipients.get(0).toString(),
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
		String pEnglish;
		String pHebrew;
		String emailText = getUnsubscribeLine();

		if (info == null) {
			category = adminService.inferCategory();
			pEnglish = adminService.inferParashaName(true, false);
			pHebrew = adminService.inferParashaName(false, true);
		} else // info came in (but parts may be missing)
		{
			if (info.category != null && info.category.length() > 0)
				category = Category.getCategory(info.category);
			else
				category = categoryRepository.findById(info.categoryId)
						.orElseThrow(() -> new ObjectNotFoundException("Category"));

			pEnglish = (info.parashaNameEnglish != null && info.parashaNameEnglish.length() > 0)
					? info.parashaNameEnglish
					: adminService.inferParashaName(true, false);

			pHebrew = info.parashaNameHebrew;

			if (info.message != null)
				emailText = concatAdminMessage(info.message, emailText);
		}

		String subject = String.format(EmailScheme.weeklyEmailSubject, pEnglish);

		List<String> usersList = userRepository.getAllUsersEmails();
		if (usersList.size() == 0)
			throw new EmailException("There are no active users, cannot send list");

		String fileNamePng = String.format(EmailScheme.weeklyFileName, utilities.formatFileName(pEnglish, "png"));
		// String fileNameHtml = String.format(EmailScheme.weeklyFileName,
		// utilities.formatFileName(pEnglish, "html"));

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText, null, usersList,
				utilities.buildListImage(category, pEnglish, pHebrew, fileNamePng), fileNamePng));

		// todo* in future - if want to send as html
		// sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText,
		// null, usersList,
		// utilities.buildListHtml(category, pEnglish, pHebrew, fileNamePng),
		// fileNamePng));

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
					davenfor.getNameHebrew(), (Category.getCategory(davenfor.getCategory()).getCname().getVisual()));
		}

		if (davenfor.getNote() != null) {
			urgentMessage = urgentMessage + utilities.toTitlecase(davenfor.getNote());
		}

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, urgentMessage, null, davenersList, null,
				null));
	}

	public void notifyUserDeletedName(Davenfor davenfor) {
		String link = getLinkToExtend(davenfor);
		String button = utilities.createSingleButton(link, "#32a842", "This name is still relevant");
		String name = davenfor.getNameEnglish().trim().length() == 0 ? davenfor.getNameHebrew()
				: davenfor.getNameEnglish();
		String message = String.format(EmailScheme.nameAutoDeletedUserMessage, name, davenfor.getCategory(), button);
		String subject = String.format(EmailScheme.nameAutoDeletedUserSubject, jwtUtils.generateOtp());

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, davenfor.getUserEmail(), null, null,
				null));
	}

	// tested
	public void informAdmin(String subject, String message) {
		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, adminEmail, null, null, null));
	}

	public boolean sendConfirmationEmail(long davenforId) throws EmailException, ObjectNotFoundException {

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor confirmedDavenfor = optionalDavenfor.get();
		String subject = EmailScheme.confirmationEmailSubject;
		String emailAddress = confirmedDavenfor.getUserEmail();

		// todo* in future - make these a method (used twice)

		String name = confirmedDavenfor.getNameEnglish().isEmpty() ? confirmedDavenfor.getNameHebrew()
				: confirmedDavenfor.getNameEnglish();

		String personalizedEmailText = String.format(EmailScheme.submitEmailText, name,
				Category.getCategory(confirmedDavenfor.getCategory()).getCname().getVisual(),
				getLinkToExtend(confirmedDavenfor), getLinkToDelete(confirmedDavenfor));

		try {
			sendEmail(createMimeMessage(sessionProvider.getSession(), subject, personalizedEmailText, emailAddress,
					null, null, null));
		} catch (Exception e) {
			throw new EmailException(String.format("Could not send confirmation email to %s", emailAddress));
		}

		return true;
	}

	// tested
	public void offerExtensionOrDelete(List<Davenfor> userDavenfors, String userEmail) {

		// this 'code' is just for differentiating emails without sending df-id or
		// showing name in the subject
		String subject = (userDavenfors.size() == 1) ? EmailScheme.expiringNameSubjectOne : EmailScheme.expiringNameSubjectMultiple;
		String message = String.format(utilities.setExpiringNameMessage(userDavenfors));

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, userEmail, null, null, null));
	}

	// tested
	public void notifydeactivatedUser(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.userdeactivated, EmailScheme.userdeactivatedSubject);
	}

	// tested
	public void notifyActivatedUser(String email) throws EmptyInformationException {
		System.out.println(getUserActivatedMessage());

		sendEmailFromAdmin(email, getUserActivatedMessage(), EmailScheme.userActivatedSubject);
	}

	public String requestToUnsubscribe(String email) {// TODO*: test
		MimeMessage mimeMessage = createMimeMessage(sessionProvider.getSession(), EmailScheme.unsubscribeSubject,
				setUnsubscribeMessage(email), email, null, null, null);
		sendEmail(mimeMessage);
		return String.format(
				"We sent you a link to complete the process. Please check your email address: %s for an 'Unsubscribe' message",
				email);
	}

	public boolean sendOtp(String email, String otp) throws EmailException {

		String subject = "Your Login Code";// put into EmailScheme

		String emailText = "Your one-time login code is: <b>" + otp
				+ "</b>. <br> Please enter this code on the website to continue. <br> <br> If you did not attempt to log in, you can safely ignore this email.";
		// put this into a file and make much nicer!

		try {
			sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText, email, null, null, null));
		} catch (Exception e) {
			throw new EmailException(String.format("Could not send otp email to %s", email));
		}

		return true;

	}

	private String concatAdminMessage(String adminMessage, String emailText) {
		// adding admin message before name and bolding it according to settings in
		// EmailScheme.
		return String.format(EmailScheme.boldFirstMessage, adminMessage, emailText);
	}

	private String setUnsubscribeMessage(String email) {
		long halfHour = 1000 * 60 * 30;
		Date expiration = new Date(new Date().getTime() + halfHour);

		String unsubscribeLink = linkToUnsubscribe + jwtUtils.generateEmailToken(email, expiration);
		return String.format(EmailScheme.unsubscribeMessage, unsubscribeLink, adminEmail);
	}

	public String getLinkToExtend(Davenfor davenfor) {
		long fiveDays = utilities.getDaysInMs(5);
		Date expiration = new Date(new Date().getTime() + fiveDays);
		String token = jwtUtils.generateEmailToken(davenfor.getUserEmail(), expiration);
		return String.format(server + linkToExtendServer, davenfor.getId(), token);
		// URLEncoder.encode(davenfor.getNameEnglish(), StandardCharsets.UTF_8),
	}

	public String getLinkToDelete(Davenfor davenfor) {
		long fiveDays = utilities.getDaysInMs(5);
		Date expiration = new Date(new Date().getTime() + fiveDays);
		String token = jwtUtils.generateEmailToken(davenfor.getUserEmail(), expiration);
		return String.format(server + linkToRemoveServer, davenfor.getId(), token);
		// URLEncoder.encode(davenfor.getNameEnglish(), StandardCharsets.UTF_8),
	}

	public String getLinkToRepost(Davenfor davenfor) {
		long twoWeeks = utilities.getDaysInMs(14);
		Date expiration = new Date(new Date().getTime() + twoWeeks);
		String token = jwtUtils.generateEmailToken(davenfor.getUserEmail(), expiration);
		return String.format(server + linkToRemoveServer, davenfor.getId(), token);
		// URLEncoder.encode(davenfor.getNameEnglish(), StandardCharsets.UTF_8),
	}

	public String getUserActivatedMessage() {
		String button = utilities.createSingleButton(client, "#32a842", "Take me to the website!");
		return "We are confirming that your participation on the Emek Hafrashat Challah Davening list has been activated. <br>" + button + "<br> You will now be receiving emails regarding the Hafrashat Challah Davening list.  You may unsubscribe at any time.  <br><br>If you did not request to join the list, please contact the list admin immediately at "
				+ adminEmail + ".<br><br>" + getUnsubscribeLine();
	}

	public String getUnsubscribeLine() {
		String linkToUnsubscribe = client + "/unsubscribe";
		return String.format(unsubscribeLine, linkToUnsubscribe);

	}

	public final String unsubscribeLine = "To unsubscribe from the Emek Hafrashat Challah Davening list, click <a href='%s'>HERE</a>";
}
