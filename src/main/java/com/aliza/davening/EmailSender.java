package com.aliza.davening;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Parasha;
import com.aliza.davening.repositories.AdminRepository;

import exceptions.EmailException;

@Component
public class EmailSender {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private AdminRepository adminRepository;

	public void sendEmailFromAdmin(String recipient, String message) throws EmailException {

		SimpleMailMessage newEmail = new SimpleMailMessage();
		newEmail.setTo(recipient);

		newEmail.setSubject("Message from Davening list admin");
		newEmail.setText(message);
		try {
			javaMailSender.send(newEmail);
		} catch (Exception e) {
			throw new EmailException("Something went wrong with sending the email.  Check the recipient's address. ");
		}

	}

	public void sendWeeklyEmailToAll(List<String> recipientList, Category category, Parasha parasha, File fileToSend)
			throws EmailException {

		MimeMessage msg = javaMailSender.createMimeMessage();

		// true = multipart message
		MimeMessageHelper helper;
		try {
			helper = new MimeMessageHelper(msg, true);
			// Converting list to array, to match MimeMessageHelper.setTo()
			String[] recipientsArray = new String[recipientList.size()];

			recipientsArray = recipientList.toArray(recipientsArray);

			helper.setTo(recipientsArray);

			// TODO: set subject in Schema.
			helper.setSubject("Weekly davening list for " + category.getEnglish());

			// default = text/plain
			// helper.setText("Check attachment for image!");

			// true = html
			// TODO: change google.com to controller/disactivateDavener(davenerId)
			helper.setText(
					"<h1>To unsubscribe from the weekly davening list, click <a href='http://www.google.com'>HERE</a></h1>",
					true);

			helper.addAttachment(category.getEnglish(), fileToSend);

			javaMailSender.send(msg);
		} catch (MessagingException e) {
			throw new EmailException("Something went wrong with sending the email.  ");
		}

	}

	public void sendUrgentEmail(List<String> recipientList, Davenfor davenfor, String davenforNote)
			throws EmailException {
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();

			// true = multipart message
			MimeMessageHelper helper = new MimeMessageHelper(msg, true);

			// Converting list to array, to match MimeMessageHelper.setTo()
			String[] recipientsArray = new String[recipientList.size()];

			recipientsArray = recipientList.toArray(recipientsArray);

			helper.setTo(recipientsArray);

			helper.setSubject("Please daven for...");

			String urgentMessage = String.format("Please daven now for %s - %s, for: %s. ", davenfor.getNameEnglish(),
					davenfor.getNameHebrew(), davenfor.getCategory().getEnglish());

			urgentMessage = urgentMessage.concat(davenforNote);

			helper.setText(urgentMessage, true);

			javaMailSender.send(msg);
		} catch (MessagingException e) {
			throw new EmailException("Something went wrong with sending the email.  ");

		}

	}

	public void informAdmin(String subject, String message) throws EmailException {
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();

			// true = multipart message
			MimeMessageHelper helper = new MimeMessageHelper(msg, true);

			helper.setTo(adminRepository.FindAdminEmailById(SchemeValues.adminId));

			helper.setSubject(subject);

			helper.setText(message, true);

			javaMailSender.send(msg);

		} catch (MessagingException e) {
			throw new EmailException("Unable to send email to Admin. ");

		}

	}

	public void sendConfirmationEmail(Davenfor davenfor) throws EmailException, IOException {
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();

			// true = multipart message
			MimeMessageHelper helper = new MimeMessageHelper(msg, true);

			helper.setTo(davenfor.getSubmitter().getEmail());

			helper.setSubject("Davening list submission");

			//TODO: turn links in email buttons to real confirm/delete links with a message 'thank you for confirming/deleting...
			
			/*
			 * Gets email text from file saved in src/resources (path defined in
			 * SchemeValues), replaces values with specific davenfor values and sets it as
			 * the email body.
			 */
			String emailText = new String(Files.readAllBytes(Paths.get(SchemeValues.confirmationEmailTextLocation)),
					StandardCharsets.UTF_8);
			String personalizedEmailText = String.format(emailText, davenfor.getNameEnglish(), davenfor.getCategory().getEnglish(), davenfor.getId());
			helper.setText(personalizedEmailText, true);

			javaMailSender.send(msg);

		} catch (MessagingException e) {
			throw new EmailException("Unable to send confirmation email to " + davenfor.getSubmitter().getEmail());

		}

	}

}
