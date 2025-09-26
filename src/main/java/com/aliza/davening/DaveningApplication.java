//The starting point of the project
package com.aliza.davening;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableEncryptableProperties
@EnableScheduling
@ServletComponentScan

public class DaveningApplication {
	public static void main(String[] args) {

		// SpringApplication.run(DaveningApplication.class, args);
		// Non-essential change to force new heroku build
		// Need headless configuration for building the image.
		SpringApplicationBuilder builder = new SpringApplicationBuilder(DaveningApplication.class);
		builder.headless(false);
		@SuppressWarnings("unused")
		ConfigurableApplicationContext context = builder.run(args);
		System.out.println("Version update: 2025-sep-09-15-08");

//		String content="didn't go through try";
//		try {
//			content = Files.readString(Path.of("src/main/resources/templates/delete-confirmation.html"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// For testing:

		// AdminService adminService = context.getBean(AdminService.class);
		// adminService.activateDavener("aliza.shanet@gmail.com");
		// SubmitterService submitterService = context.getBean(SubmitterService.class);
		// CategoryRepository categoryRep = context.getBean(CategoryRepository.class);
		// EmailSender es = context.getBean(EmailSender.class);

		// es.offerExtensionOrDelete(new Davenfor(5, "aliza.shanet@gmail.com",
		// categoryRep.getOne(1L), "nameHebrew", "nameEnglish", null, null, true,
		// LocalDate.now(), LocalDate.now(),LocalDate.now(),LocalDate.now(),"test note"
		// ));
	}

	// For encoding user passwords - rest of application needs this (leave it!)
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}