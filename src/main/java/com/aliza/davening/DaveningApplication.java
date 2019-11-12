package com.aliza.davening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.aliza.davening.services.AdminService;

import exceptions.ObjectNotFoundException;

@SpringBootApplication
public class DaveningApplication {

//TODO: remove unnecessary comments. 
	// static {
	// Setup.buildCategories();
	// }

	public static void main(String[] args) throws ObjectNotFoundException {

		ConfigurableApplicationContext context = SpringApplication.run(DaveningApplication.class, args);

		AdminService adminService = context.getBean(AdminService.class);

//	System.out.println(adminService.login("admin@gmail.com", "admin1234"));
	}

}
