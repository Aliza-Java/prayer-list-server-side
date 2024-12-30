package com.aliza.davening.services;

import com.aliza.davening.exceptions.EmailException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public interface EmailService {
	public boolean sendEmail(MimeMessage message) throws MessagingException, EmailException;
}