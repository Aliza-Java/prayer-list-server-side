package com.aliza.davening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.aliza.davening.entities.Davener;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
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
		DavenerRepository davenerRep = context.getBean(DavenerRepository.class);
		DavenforRepository davenforRep = context.getBean(DavenforRepository.class);

//		Category shidduch = new Category(" categr", true, 2, 40);
//		adminService.createCategory(shidduch);
//		 System.out.println(adminService.getAllDavenfors());

//		 adminService.deleteDavenfor(6);

//		Admin admin = new Admin();
//		admin.setPassword("222222222222");
//		admin.setEmail("what2@isit.com");
//		adminService.createAdmin(admin);
//		
//	Submitter submitter = new Submitter();
//		submitter.setEmail("eggfooyoung@wrongdomain.hmm");
//		submitter.setWhatsapp("99");
//		submitter.setId(100);
//		submitter.setName("it's me-");
//		adminService.createSubmitter(submitter);
////		
//		Davenfor d1 = new Davenfor();
//		d1.setNameEnglish(" English name");
//		d1.setNameHebrew("השם בעברית");
//		d1.setNameHebrewSpouse("הבעל בעברית");
//		d1.setNameEnglishSpouse("Husband in English");
//		d1.setExpireAt("2009-04-11");
//	
//		d1.setCategory(adminService.getCategory(2));
//		d1.setSubmitter(submitter);
//		d1.setSubmitterToReceive(false);
//		
//
//		adminService.createDavenfor(d1);

//		adminService.deleteCategory(8);
//		adminService.deleteDavenfor(13);

//		System.out.println(adminService.getDavenfor(14).getSubmitter().getName());
//		adminService.updateSubmitterName("New submitter name");
//		System.out.println(adminService.getDavenfor(14).getSubmitter().getName());

//		System.out.println(adminService.getDavenfor(16).getCategory());
//		adminService.saveUpdatedCategory();
//		System.out.println(adminService.getDavenfor(16).getCategory());
		
//		Davener davener = new Davener();
//		davener.setEmail("davener3@gmail.com");
//		
//		adminService.createDavener(davener);
//		System.out.println(davenerRep.getAllDavenersEmails());
		
//		davenerRep.disactivateDavener(2);
		
//		davenforRep.extendExpiryDate("2009-05-11", 1);
		davenforRep.setLastConfirmedAt("2019-11-06", 1);

	}
}
