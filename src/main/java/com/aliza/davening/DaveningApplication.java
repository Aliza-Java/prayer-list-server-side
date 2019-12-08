package com.aliza.davening;

import java.io.IOException;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.aliza.davening.services.AdminService;

import exceptions.DatabaseException;
import exceptions.EmailException;
import exceptions.EmptyInformationException;
import exceptions.ObjectNotFoundException;

@SpringBootApplication
public class DaveningApplication {

	@Autowired
	EmailSender emailSender;

	public static void main(String[] args) throws ObjectNotFoundException, EmptyInformationException, DatabaseException,
			IOException, MessagingException, EmailException {

		ConfigurableApplicationContext context = SpringApplication.run(DaveningApplication.class, args);

		AdminService adminService = context.getBean(AdminService.class);
		// SubmitterService submitterService = context.getBean(SubmitterService.class);

//		DavenforRepository davenforRep = context.getBean(DavenforRepository.class);
//		DavenerRepository davenerRep = context.getBean(DavenerRepository.class);
//		SubmitterRepository submitterRep = context.getBean(SubmitterRepository.class);
//		CategoryRepository categoryRep = context.getBean(CategoryRepository.class);

//		try {
//			submitterService.deleteDavenfor(15, "thursSubmitter@gmail.com");
//		} catch (PermissionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		Davenfor dfToUpdate = davenforRep.findById((long) 17).get();
//	
//		dfToUpdate.setCategory(categoryRep.findById((long) 10).get());
//	
//		try {
//			submitterService.updateDavenfor(dfToUpdate, "weDSubmitter@gmail.com");
//		} catch (PermissionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		try {
//			submitterService.extendDavenfor(10, "chooseme@gmail.com");
//		} catch (PermissionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		Davenfor wednesdayDavenfor = new Davenfor(1000, null,
//				categoryRep.findById((long) 9).get(), "חולה בן  אמא שלו", "choleh ben Ima  ", null, null, true, null, null,
//				null, null);
//
//		submitterService.addDavenfor(wednesdayDavenfor, "wedSubmitter@gmail.com");

//		List<Davenfor> davenfors = submitterService.getAllSubmitterDavenfors("weDSubmitter@gmail.com");
//		for(Davenfor d:davenfors) {
//			System.out.println(d);
//		}
		// System.out.println(adminService.getAllCategories());

		// Category tryToPushIn = new Category(3, " newnamE", false, 88, 150);
		// Category tryToPushIn = null;
		// adminService.updateCategory(2, tryToPushIn);
		// adminService.addCategory(null);

		// adminService.deleteCategory(3);

		// System.out.println(adminService.getAllCategories());
		// Parasha bereishit = new Parasha(1, "Bereishit", "בראשית", "bereishit");
		// adminService.sendOutWeekly(bereishit);

		// adminService.sendOutUrgent(davenforRep.findById((long) 6).get(), "Very
		// urgent. ");
		// adminService.disactivateDavener(davenerRep.findById((long) 4).get());
		// System.out.println(davenerRep.getAllDavenersEmails());

		// Category current = categoryRep.findById((long) 11).get();
		// System.out.println(adminService.getNextCategory(current));

		// System.out.println(submitterRep.findByEmail("submitter8@gmail.com"));
		// System.out.println(SchemeValues.createConfirmationEmailText());

		// submitterService.emailConfirmation(davenforRep.findById((long) 15).get());

	}

}
