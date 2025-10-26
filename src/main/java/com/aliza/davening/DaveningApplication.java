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
		System.out.println("Version update: 2025-oct-26-1-25 - limit list size while sorting shidduchim");

//		String content="didn't go through try";
//		try {
//			content = Files.readString(Path.of("src/main/resources/templates/delete-confirmation.html"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// For testing:

		//MaintainList ml = context.getBean(MaintainList.class);
		//ml.updateNewWeek();
		//ml.offerExtensionOrDelete();
		// SubmitterService submitterService = context.getBean(SubmitterService.class);
		// CategoryRepository categoryRep = context.getBean(CategoryRepository.class);

		 //ml.deleteUnconfirmed();
	}

	// For encoding user passwords - rest of application needs this (leave it!)
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}