package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailScheme;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.CategoryType;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.services.session.EmailSessionProvider;
import com.aliza.davening.util_classes.Weekly;

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

	private List<String> adminAsList;

	@PostConstruct
	public void init() {
		adminAsList = Collections.singletonList(adminEmail);
	}

	public static MimeMessage createMimeMessage(Session session, String subject, String text, String to,
			List<String> bccList, File attachment, String attachmentName) {

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
			textPart.setText(text);
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
		// TODO: allow attachment, make all methods use this, services and controllers
		// to direct to EmailSender correctly.

		try {
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
	// tested
	public void sendEmailFromAdmin(String recipient, String text) throws EmptyInformationException {

		if (recipient == null || recipient.length() == 0) {
			throw new EmptyInformationException("Recipient email address missing.");
		}

		Session session = sessionProvider.getSession();
		MimeMessage mimeMessage = createMimeMessage(session, EmailScheme.getAdminMessageSubject(), text, recipient,
				adminAsList, null, null);

		sendEmail(mimeMessage);
	}

	// tested
	public void sendSimplifiedWeekly() throws IOException, ObjectNotFoundException, EmptyInformationException {
		Weekly simplified = new Weekly();

		Parasha parasha = parashaRepository.findCurrent()
				.orElseThrow(() -> new ObjectNotFoundException("current Parasha"));
		simplified.parashaName = parasha.getEnglishName();

		simplified.category = categoryRepository.getCurrent()
				.orElseThrow(() -> new ObjectNotFoundException("current category"));

		sendOutWeekly(simplified);
	}

	// tested
	public void sendOutWeekly(Weekly info) throws IOException, ObjectNotFoundException, EmptyInformationException {

		Category category;
		if (info.category != null)
			category = info.category;
		else
			category = categoryRepository.findById(info.categoryId)
					.orElseThrow(() -> new ObjectNotFoundException("category"));

		LocalDate date = LocalDate.now();
		String todaysDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(date);
		String parashaName = (info.parashaName != null && info.parashaName.length() > 0)
				? "Parashat " + info.parashaName + " - " + todaysDate
				: todaysDate;
		String subject = String.format(EmailScheme.getWeeklyEmailSubject(), parashaName);
		String emailText = EmailScheme.getWeeklyEmailText();

		// If there is a message from the admin, add it beforehand.
		if (info.message != null) {
			emailText = concatAdminMessage(info.message, emailText);
		}

		List<String> davenersList = davenerRepository.getAllDavenersEmails();

		String fileName = String.format(EmailScheme.getWeeklyFileName(), parashaName);

		// 'to' field in doEmail cannot be empty (JavaMailSender in subsequent methods),
		// therefore including admin's email.

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, emailText, adminEmail, davenersList,
				utilities.buildListImage(category, info.parashaName), fileName));
	}

	// tested
	public void sendUrgentEmail(Davenfor davenfor) throws EmptyInformationException {

		if (davenfor == null) {
			throw new EmptyInformationException("The name you submitted for davening is incomplete.  ");
		}

		List<String> davenersList = davenerRepository.getAllDavenersEmails();

		// Specifying the name in order to avoid Gmail from bunching up many urgent
		// names under a single email thread.
		String subject = String.format("Please daven for %s", davenfor.getNameEnglish());

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

		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, urgentMessage, adminEmail, davenersList,
				null, null));
	}

	// TODO: how to implement the html style? it gets sent as plain text
	// tested
	public void informAdmin(String subject, String message) {
		sendEmail(createMimeMessage(sessionProvider.getSession(), subject, message, adminEmail, null, null, null));
	}

	// TODO - when ready, make someone call this method
	// The controller will use this method to send out a confirmation email to
	// submitter when sending in a new name. public boolean
	/*
	 * boolean sendConfirmationEmail(long davenforId) //TODO - make someone call
	 * this, if want. Or delete. throws EmailException, IOException,
	 * EmptyInformationException, ObjectNotFoundException {
	 * 
	 * Optional<Davenfor> optionalDavenfor =
	 * davenforRepository.findById(davenforId); if (!optionalDavenfor.isPresent()) {
	 * throw new ObjectNotFoundException("Name with id: " + davenforId); }
	 * 
	 * Davenfor confirmedDavenfor = optionalDavenfor.get(); String subject =
	 * EmailScheme.getConfirmationEmailSubject();
	 * 
	 * 
	 * Retrieving standard confirmation email text, and personalizing it
	 * respectively. Code gets email text from file saved in src/resources (path
	 * defined in SchemeValues), replaces values with specific davenfor values and
	 * sets it as the email body.
	 * 
	 * 
	 * String emailText = new String(Files.readAllBytes(Paths.get(EmailScheme.
	 * getConfirmationEmailTextLocation())), StandardCharsets.UTF_8); String
	 * personalizedEmailText = String.format(emailText,
	 * confirmedDavenfor.getNameEnglish(),
	 * confirmedDavenfor.getCategory().getCname(), confirmedDavenfor.getId());
	 * 
	 * String to = confirmedDavenfor.getSubmitterEmail();
	 * 
	 * try { sendEmail(createMimeMessage(sessionProvider.getSession(), subject,
	 * personalizedEmailText, to, adminAsList, null, null)); } catch (Exception e) {
	 * throw new EmailException(
	 * 
	 * String.format("Could not send confirmation email to %s",
	 * confirmedDavenfor.getSubmitterEmail())); } return true;
	 * 
	 * }
	 */

	// tested
	public void notifyDisactivatedDavener(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.getDavenerDisactivated());
	}

	// tested
	public void notifyActivatedDavener(String email) throws EmptyInformationException {
		sendEmailFromAdmin(email, EmailScheme.getDavenerActivated());
	}

	// tested
	public void offerExtensionOrDelete(Davenfor davenfor) {

		String subject = EmailScheme.getExpiringNameSubject();
		String message = String.format(Utilities.setExpiringNameMessage(davenfor));
		String recipient = davenfor.getSubmitterEmail();

		sendEmail(
				createMimeMessage(sessionProvider.getSession(), subject, message, recipient, adminAsList, null, null));
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
}
