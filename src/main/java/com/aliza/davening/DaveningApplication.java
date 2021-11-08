package com.aliza.davening;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.NoRelatedEmailException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.exceptions.ReorderCategoriesException;
import com.itextpdf.text.DocumentException;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableEncryptableProperties
@EnableScheduling
@ServletComponentScan

public class DaveningApplication {

//	@Autowired
//	EmailSender emailSender;

	public static void main(String[] args) throws ObjectNotFoundException, EmptyInformationException, DatabaseException,
			IOException, MessagingException, EmailException, NoRelatedEmailException, ReorderCategoriesException,
			DocumentException, PermissionException {

		//SpringApplication.run(DaveningApplication.class, args);
		
		//Need headless configuration for building the image.
		SpringApplicationBuilder builder = new SpringApplicationBuilder(DaveningApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		

		// For testing:

		// SubmitterService submitterService = context.getBean(SubmitterService.class);
		// emailSender.sendOutWeekly(Utilities.findParasha(9), "specific text");
	}

	// For encoding user passwords - rest of application needs this (leave it!)
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}