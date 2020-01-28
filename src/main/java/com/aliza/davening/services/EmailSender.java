package com.aliza.davening.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.aliza.davening.EmailScheme;
import com.aliza.davening.SchemeValues;
import com.aliza.davening.Utilities;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.itextpdf.text.DocumentException;

import exceptions.DatabaseException;
import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.ObjectNotFoundException;

@Service
public class EmailSender {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private DavenforRepository davenforRepository;

	@Autowired
	private DavenerRepository davenerRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	// A general method allowing Admin to send messages to system users
	public void sendEmailFromAdmin(String recipient, String message) throws EmailException, EmptyInformationException {

		if (recipient == null) {
			throw new EmptyInformationException("Recipient email address missing. ");
		}

		String[] recipientAsArray = { recipient };

		/*
		 * simple email where subject is defined in EmailScheme, message is received,
		 * recipient is only one, and there is no bcc.
		 */
		doEmail(EmailScheme.getAdminMessageSubject(), message, recipientAsArray, null, null, null);
	}

	public void sendOutWeekly(Parasha parasha, String message) throws EmptyInformationException, IOException,
			MessagingException, EmailException, DocumentException, ObjectNotFoundException, DatabaseException {
		if (parasha == null) {
			throw new EmptyInformationException("No Parasha name submitted. ");
		}

		Category currentCategory = categoryRepository.getCurrent();

		String subject = String.format(EmailScheme.getWeeklyEmailSubject(), currentCategory.getEnglish());

		String emailText = EmailScheme.getWeeklyEmailText();

		// If there is a message from the admin, add it beforehand.
		if (message != null) {
			emailText = concatAdminMessage(message, emailText);
		}

		List<String> davenersList = davenerRepository.getAllDavenersEmails();

		// Converting list to array, to match MimeMessageHelper.setTo()
		String[] davenersArray = new String[davenersList.size()];
		davenersArray = davenersList.toArray(davenersArray);

		String fileName = String.format(EmailScheme.getWeeklyFileName(), parasha.getEnglishName());

		doEmail(subject, message, null, davenersArray, Utilities.buildListImage(parasha), fileName);
	}

	public void sendUrgentEmail(List<String> recipientList, Davenfor davenfor, String davenforNote)
			throws EmailException, EmptyInformationException {

		if (davenfor == null) {
			throw new EmptyInformationException("The name you submitted for davening is incomplete.  ");
		}

		// Converting list to array, to match MimeMessageHelper.setTo()
		String[] recipientsArray = new String[recipientList.size()];
		recipientsArray = recipientList.toArray(recipientsArray);

		// Specifying the name in order to avoid Gmail from bunching up many urgent
		// names under a single email thread.
		String subject = String.format("Please daven for %s", davenfor.getNameEnglish());

		// defining urgentMessage at start of method, outside 'if' clause, so that it is
		// recognized in all parts of the method.
		String urgentMessage;

		// If category is banim, need to list also spouse name (if exists in at least
		// one language).
		if (SchemeValues.banimName.equals(davenfor.getCategory().getEnglish())
				&& davenfor.getNameEnglishSpouse() != null || davenfor.getNameHebrewSpouse() != null) {
			urgentMessage = String.format(EmailScheme.getUrgentDavenforEmailBanim(), davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getNameEnglishSpouse(), davenfor.getNameHebrewSpouse(),
					SchemeValues.banimName);
		}

		else {
			urgentMessage = String.format(EmailScheme.getUrgentDavenforEmail(), davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getCategory().getEnglish());
		}

		if (davenforNote != null) {
			urgentMessage = concatAdminMessage(davenforNote, urgentMessage);
		}

		doEmail(subject, urgentMessage, null, recipientsArray, null, null);
	}

	public void informAdmin(String subject, String message) throws EmailException {

		String[] toAdmin = { adminRepository.FindAdminEmailById(SchemeValues.adminId) };

		doEmail(subject, message, toAdmin, null, null, null);

	}

	// The controller will use this meathod to send out a confirmation email to
	// submitter when sending in a new name.
	public boolean sendConfirmationEmail(long davenforId)
			throws EmailException, IOException, EmptyInformationException, ObjectNotFoundException {

		Optional<Davenfor> optionalDavenfor = davenforRepository.findById(davenforId);
		if (optionalDavenfor.isEmpty()) {
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
		// TODO: Once controllers in place turn links in email buttons to real
		// confirm/delete links with a message
		// 'thank you for confirming/deleting...'

		String emailText = new String(Files.readAllBytes(Paths.get(EmailScheme.getConfirmationEmailTextLocation())),
				StandardCharsets.UTF_8);
		String personalizedEmailText = String.format(emailText, confirmedDavenfor.getNameEnglish(),
				confirmedDavenfor.getCategory().getEnglish(), confirmedDavenfor.getId());

		String[] to = { confirmedDavenfor.getSubmitter().getEmail() };

		try {
			doEmail(subject, personalizedEmailText, to, null, null, null);

		} catch (Exception e) {
			throw new EmailException(String.format("Could not send confirmation email to %s",
					confirmedDavenfor.getSubmitter().getEmail()));
		}
		return true;

	}

	public void notifyDisactivatedDavener(String email) throws EmailException, EmptyInformationException {

		sendEmailFromAdmin(email, EmailScheme.getDavenerDisactivated());
	}

	private void doEmail(String subject, String message, String[] to, String[] bcc, File attachment,
			String attachmentName) throws EmailException {
		MimeMessage msg = javaMailSender.createMimeMessage();

		MimeMessageHelper helper;
		try {
			// true = multipart message
			helper = new MimeMessageHelper(msg, true);

			helper.setSubject(subject);

			// true = html. Default: text/plain
			helper.setText(message, true);

			helper.setTo(to);
			helper.setBcc(bcc);
			if (attachment != null) {
				helper.addAttachment(attachmentName, attachment);
			}
			javaMailSender.send(msg);
		} catch (Exception e) {
			throw new EmailException("Something went wrong with sending the email.  " + e);
		}

	}

	private String concatAdminMessage(String adminMessage, String emailText) {
		// adding admin message first and bolding it according to settings in
		// EmailScheme.
		return String.format(EmailScheme.getBoldFirstMessage(), adminMessage, emailText);
	}

}
