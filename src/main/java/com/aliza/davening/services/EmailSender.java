package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailScheme;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.CategoryType;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.util_classes.Weekly;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSender implements EmailService {

	@Autowired
    private EmailSessionProvider sessionProvider;
	
	@Autowired
	private DavenforRepository davenforRepository;

	@Autowired
	private DavenerRepository davenerRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ParashaRepository parashaRepository;

	@Autowired
	private Utilities utilities;

	@Value("${admin.email}")
	String adminEmail;

	// @Value("${admin.id}")
	long adminId = 1;

	public static MimeMessage createMimeMessage(Session session, String subject, String text, String to, String[] bcc,
			File attachment, String attachmentName) {

		// Create the email message
		MimeMessage message = new MimeMessage(session);
		try {
			message.setRecipients(Message.RecipientType.TO, to);
			message.setSubject(subject);
			message.setText(text);
		} catch (MessagingException e) {
			throw new RuntimeException("Failed to create MIME message", e);
		}

		return message;
	}

	// general method
	@Override
	public boolean sendEmail(MimeMessage message) throws MessagingException, EmailException {
		// TODO: allow attachment, make all methods use this, services and controllers
		// to direct to EmailSender correctly.

		try {
			// Send the email
			Transport.send(message);
			System.out.println("Email sent successfully!");
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to send email");
		}

		// TODO - where to put the encoding utf-8?
		// TODO - mimeMessageHelper for attachment and more

		return true;
	}

	// A general method allowing Admin to send messages to system users public
	public void sendEmailFromAdmin(String recipient, String text)
			throws EmailException, EmptyInformationException, MessagingException {

		if (recipient == null) {
			throw new EmptyInformationException("Recipient email address missing.");
		}

		Session session = sessionProvider.getSession();
		
		MimeMessage mimeMessage = createMimeMessage(session, EmailScheme.getAdminMessageSubject(), text, recipient,
				makeAdminTheBcc(), null, null);

		sendEmail(mimeMessage);
	}

	public void sendSimplifiedWeekly() throws IOException, MessagingException, EmailException, ObjectNotFoundException,
			DatabaseException, EmptyInformationException {
		Weekly simplified = new Weekly();
		simplified.parashaName = parashaRepository.findCurrent().getEnglishName();
		simplified.fullWeekName = parashaRepository.findCurrent().getEnglishName() + " - "
				+ parashaRepository.findCurrent().getHebrewName();
		simplified.categoryId = categoryRepository.getCurrent().getId();
		simplified.message = null;
		sendOutWeekly(simplified);
	}

	public void sendOutWeekly(Weekly info) throws IOException, MessagingException, EmailException,
			ObjectNotFoundException, DatabaseException, EmptyInformationException {

		Optional<Category> optionalCategory = categoryRepository.findById(info.categoryId);
		if (!optionalCategory.isPresent()) {
			throw new ObjectNotFoundException("category of id " + info.categoryId);
		}
		Category category = optionalCategory.get();

		LocalDate date = LocalDate.now();
		String todaysDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(date);

		String subject = String.format(EmailScheme.getWeeklyEmailSubject(),
				(info.parashaName != null && info.parashaName.length() > 0) ? info.parashaName : todaysDate);

		String emailText = EmailScheme.getWeeklyEmailText();

		// If there is a message from the admin, add it beforehand.
		if (info.message != null) {
			emailText = concatAdminMessage(info.message, emailText);
		}

		List<String> davenersList = davenerRepository.getAllDavenersEmails();

		// Converting list to array, to match MimeMessageHelper.setTo()
		String[] davenersArray = new String[davenersList.size()];
		davenersArray = davenersList.toArray(davenersArray);

		String fileName = String.format(EmailScheme.getWeeklyFileName(), todaysDate);

		// 'to' field in doEmail cannot be empty (JavaMailSender in subsequent methods),
		// therefore including admin's email.

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText, adminEmail, davenersArray,
				utilities.buildListImage(category, info.parashaName, info.fullWeekName), fileName));
	}

	public void sendUrgentEmail(Davenfor davenfor)
			throws EmailException, EmptyInformationException, MessagingException {

		if (davenfor == null) {
			throw new EmptyInformationException("The name you submitted for davening is incomplete.  ");
		}

		List<String> davenersList = davenerRepository.getAllDavenersEmails();

		// Converting list to array, to match MimeMessageHelper.setTo()
		String[] davenersArray = new String[davenersList.size()];
		davenersArray = davenersList.toArray(davenersArray);

		// Specifying the name in order to avoid Gmail from bunching up many urgent
		// names under a single email thread.
		String subject = String.format("Please daven for %s", davenfor.getNameEnglish());

		// defining urgentMessage at start of method, outside 'if' clause, so that it is
		// recognized in all parts of the method.
		String urgentMessage;

		// If category is banim, need to list also spouse name (if exists in at least
		// one language).
		if (CategoryType.BANIM.equals(davenfor.getCategory().getCname())
				&& (davenfor.getNameEnglishSpouse() != null || davenfor.getNameHebrewSpouse() != null)) {
			urgentMessage = String.format(EmailScheme.getUrgentDavenforEmailBanim(), davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getNameEnglishSpouse(), davenfor.getNameHebrewSpouse(),
					SchemeValues.banimName);
		}

		else {
			urgentMessage = String.format(EmailScheme.getUrgentDavenforEmailText(), davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getCategory().getCname());
		}

		if (davenfor.getNote() != null) {
			urgentMessage = concatAdminMessageAfter(davenfor.getNote(), urgentMessage);
		}

		// 'to' field in doEmail cannot be empty (JavaMailSender in subsequent methods),
		// therefore including admin's email.

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, urgentMessage, adminEmail, davenersArray, null, null));
	}

	public void informAdmin(String subject, String message) throws EmailException, MessagingException {

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, adminEmail, makeAdminTheBcc(), null, null));

	}

	// The controller will use this meathod to send out a confirmation email to
	// submitter when sending in a new name. public boolean
	boolean sendConfirmationEmail(long davenforId)
			throws EmailException, IOException, EmptyInformationException, ObjectNotFoundException {

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (!optionalDavenfor.isPresent()) {
			throw new ObjectNotFoundException("Name with id: " + davenforId);
		}

		Davenfor confirmedDavenfor = optionalDavenfor.get();
		String subject = EmailScheme.getConfirmationEmailSubject();

		/*
		 * Retrieving standard confirmation email text, and personalizing it
		 * respectively. Code gets email text from file saved in src/resources (path
		 * defined in SchemeValues), replaces values with specific davenfor values and
		 * sets it as the email body.
		 */

		String emailText = new String(Files.readAllBytes(Paths.get(EmailScheme.getConfirmationEmailTextLocation())),
				StandardCharsets.UTF_8);
		String personalizedEmailText = String.format(emailText, confirmedDavenfor.getNameEnglish(),
				confirmedDavenfor.getCategory().getCname(), confirmedDavenfor.getId());

		String to = confirmedDavenfor.getSubmitterEmail();

		try {
			sendEmail(
					createMimeMessage(sessionProvider.getSession(), subject, personalizedEmailText, to, makeAdminTheBcc(), null, null));
		} catch (Exception e) {
			throw new EmailException(

					String.format("Could not send confirmation email to %s", confirmedDavenfor.getSubmitterEmail()));
		}
		return true;

	}

	public void notifyDisactivatedDavener(String email) throws EmptyInformationException, MessagingException {

		try {
			sendEmailFromAdmin(email, EmailScheme.getDavenerDisactivated());
		} catch (EmailException e) { // for now just notify in log.
			System.out.println("There was a problem sending the email.");
		}
	}

	public void notifyActivatedDavener(String email) throws EmptyInformationException, MessagingException {
		try {
			sendEmailFromAdmin(email, EmailScheme.getDavenerActivated());
		} catch (EmailException e) { // for now just notify in log.
			System.out.println("There was a problem sending the email.");
		}
	}

	public void offerExtensionOrDelete(Davenfor davenfor) throws EmailException, MessagingException {

		String subject = EmailScheme.getExpiringNameSubject();
		String message = String.format(Utilities.setExpiringNameMessage(davenfor));
		String recipient = davenfor.getSubmitterEmail();

		try {
			sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, recipient, makeAdminTheBcc(), null, null));
		} catch (EmailException e) {
			String.format("Unable to send an email to %s offering to extend or delete the name %s.", recipient,
					davenfor.getNameEnglish());
			e.printStackTrace();
		}
	}

	private String concatAdminMessage(String adminMessage, String emailText) {
		// adding admin message before name and bolding it according to settings in
		// EmailScheme.
		return String.format(EmailScheme.getBoldFirstMessage(), adminMessage, emailText);
	}

	private String concatAdminMessageAfter(String adminMessage, String emailText) {
		// adding admin message after name and bolding it according to settings in
		// EmailScheme.
		return String.format(EmailScheme.getBoldSecondMessage(), emailText, adminMessage);
	}

	private String[] makeAdminTheBcc() {
		String[] adminEmailAsArray = { adminEmail };
		return adminEmailAsArray;
	}

}
