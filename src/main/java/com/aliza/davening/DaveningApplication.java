package com.aliza.davening;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.aliza.davening.services.AdminService;
import com.itextpdf.text.DocumentException;

import exceptions.DatabaseException;
import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.NoRelatedEmailException;
import exceptions.ObjectNotFoundException;
import exceptions.PermissionException;
import exceptions.ReorderCategoriesException;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
public class DaveningApplication {

//	@Autowired
//	EmailSender emailSender;

	public static void main(String[] args)
			throws ObjectNotFoundException, EmptyInformationException, DatabaseException, IOException,
			MessagingException, EmailException, NoRelatedEmailException, ReorderCategoriesException, DocumentException, PermissionException {

		// Using this instead of the common SpringApplication.run (Application.class,
		// args) because the Swing frame that has to do with html2image is causing a
		// java.awt.HeadlessException
		SpringApplicationBuilder builder = new SpringApplicationBuilder(DaveningApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);

		AdminService adminService = context.getBean(AdminService.class);
		
	}

}
