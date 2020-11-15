package com.aliza.davening;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
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
import com.aliza.davening.services.SubmitterService;
import com.itextpdf.text.DocumentException;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableEncryptableProperties
@EnableScheduling

public class DaveningApplication {

//	@Autowired
//	EmailSender emailSender;

	public static void main(String[] args) throws ObjectNotFoundException, EmptyInformationException, DatabaseException,
			IOException, MessagingException, EmailException, NoRelatedEmailException, ReorderCategoriesException,
			DocumentException, PermissionException {

		
		// Needed the below alternative for Utilities.buildListImage
		 SpringApplicationBuilder builder = new SpringApplicationBuilder(DaveningApplication.class);
		 builder.headless(false);
		 ConfigurableApplicationContext context = builder.run(args);

		// For testing:
		
		 //SubmitterService submitterService = context.getBean(SubmitterService.class);
		 //emailSender.sendOutWeekly(Utilities.findParasha(9), "What is headless anyway?");
		 

	}
	

	

	//For encoding user passwords
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
