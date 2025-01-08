//The starting point of the project
package com.aliza.davening;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.aliza.davening.entities.CategoryType;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.DatabaseException;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.NoRelatedEmailException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.exceptions.PermissionException;
import com.aliza.davening.exceptions.ReorderCategoriesException;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.SubmitterService;
import com.aliza.davening.services.session.EmailSessionProvider;
import com.aliza.davening.services.session.ProdEmailSessionProvider;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import jakarta.mail.MessagingException;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableEncryptableProperties
@EnableScheduling
@ServletComponentScan

public class DaveningApplication {
	public static void main(String[] args) throws ObjectNotFoundException, EmptyInformationException, DatabaseException,
			IOException, MessagingException, EmailException, NoRelatedEmailException, ReorderCategoriesException,
			PermissionException {

		//SpringApplication.run(DaveningApplication.class, args);
		//Non-essential change to force new heroku build
		//Need headless configuration for building the image.
		SpringApplicationBuilder builder = new SpringApplicationBuilder(DaveningApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);	
		
		// For testing:
		
		//AdminService adminService = context.getBean(AdminService.class);
		//adminService.activateDavener("aliza.shanet@gmail.com");
		// SubmitterService submitterService = context.getBean(SubmitterService.class);
		//CategoryRepository categoryRep = context.getBean(CategoryRepository.class);
		//EmailSender es = context.getBean(EmailSender.class);
		
		 //es.offerExtensionOrDelete(new Davenfor(5, "aliza.shanet@gmail.com", categoryRep.getOne(1L), "nameHebrew", "nameEnglish", null, null, true, LocalDate.now(),  LocalDate.now(),LocalDate.now(),LocalDate.now(),"test note" ));
	}

	// For encoding user passwords - rest of application needs this (leave it!)
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}