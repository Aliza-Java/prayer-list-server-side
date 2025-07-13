//Scheduled actions to maintain the list
package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.SHIDDUCHIM;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.CategoryName;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;

@Component
public class MaintainList { // TODO*: tests for all these

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	AdminService adminService;

	@Autowired
	EmailSender emailSender;

	@Autowired
	Utilities utilities;

	@Autowired
	ParashaRepository parashaRepository;

	@Autowired
	CategoryRepository categoryRepository;

//	@Autowired
//	EmailScheme emailScheme;

	@Value("${admin.id}")
	private long adminId;

	// Every Sunday at 2 a.m. changes category and parasha
	@Scheduled(cron = "0 0 2 * * SUN")
	public void updateNewWeek() {
		System.out.println("Begin updateNewWeek().  Current category: " + adminService.findCurrentCategory()
				+ ", current parasha: " + adminService.findCurrentParasha());
		adminService.updateParasha();
		adminService.updateCurrentCategory();
		Category currentCategory = adminService.findCurrentCategory();
		davenforRepository.clearConfirmedAt(currentCategory.getCname().toString());
		System.out.println("End updateNewWeek().  New category: " + currentCategory.getCname().getVisual() + ", new parasha: "
				+ adminService.findCurrentParasha().getEnglishName());
	}

	// for each category going to be sent out, Sends email to davenfor's submitter
	// to ask if name in category is still relevant,
	// Fires right after changing category and parasha
	@Scheduled(cron = "0 5 2 * * SUN")
	public void offerExtensionOrDelete() {
		System.out.println("Begin offerExtensionOrDelete()");
		// List<Davenfor> expiredDavenfors =
		// davenforRepository.findByExpireAtLessThan(LocalDate.now());
		Category currentCategory = categoryRepository.getCurrent().get();
		CategoryName categoryName = currentCategory.getCname();
		//At first Lynne said these not all the time, then she said yes
		//if (categoryName.equals(SHIDDUCHIM) || categoryName.equals(BANIM))
		//	return;

		List<Davenfor> relevantDavenfors = davenforRepository.findAllDavenforByCategory(categoryName.toString());

		System.out.println("Davenfors in question: " + relevantDavenfors.size());
		System.out.println(relevantDavenfors);
		for (Davenfor d : relevantDavenfors) {
			emailSender.offerExtensionOrDelete(d);
			System.out.println("emailed " + d.getUserEmail() + " to offerExtensionOrDelete for df id " + d.getId());
		}
		System.out.println("End offerExtensionOrDelete()");
	}

	// 'Deletes' davenfors which have not been confirmed, according to category that
	// will be sent to admin
	@Scheduled(cron = "0 55 7 * * WED")
	@Transactional
	public void deleteUnconfirmed() {
		// TODO*: when add more admins, will need to add admin_id to each davenfor
		// (unless in different DBs?)
		System.out.println("Begin deleteUnconfirmed()");

		Category category = adminService.findCurrentCategory();
		String categoryDb = category.getCname().toString();
		System.out.println(String.format("Starting with %d davenfors in the %s category. ", davenforRepository.findAllDavenforByCategory(categoryDb).size(), categoryDb));
		List<Davenfor> unconfirmed = davenforRepository.findAllByCategoryAndConfirmedAtIsNull(categoryDb);
		
		if (unconfirmed.size() > 0)
		{
		unconfirmed.forEach(u -> {
			davenforRepository.softDeleteById(u.getId());
			emailSender.notifyUserDeletedName(u);
		});
		emailSender.informAdmin(EmailScheme.unconfirmedSubject, EmailScheme
				.createUnconfirmedMessage(category.getCname().getVisual(), unconfirmed));

		System.out.println(String.format("End deleteUnconfirmed() - now have %d davenfors in the %s category. ", davenforRepository.findAllDavenforByCategory(categoryDb).size(), categoryDb));
		}
		else
			System.out.println("End deleteUnconfirmed().  No relevant davenfors were found to delete.");
	}

	// Every Wednesday at 7 a.m. an email will be sent to Admin with a link to see
	// list and send out, with link to his login page.
	@Scheduled(cron = "0 0 8 * * WED")
	public void remindAdmin() {
		System.out.println("Begin remindAdmin()");
		emailSender.informAdmin(EmailScheme.weeklyAdminReminderSubject, utilities.setWeeklyAdminReminderMessage());
		System.out.println("End remindAdmin()");
	}
}
