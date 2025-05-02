//Scheduled actions to maintain the list
package com.aliza.davening;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;

@Component
public class MaintainList { //TODO*: tests for all these

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

	@Value("${admin.id}")
	private long adminId;

	// Deletes davenfors which have expired, even past the grace period of
	// wait.before.deletion
	// Fires at 1 a.m. every day
	@Scheduled(cron = "0 0 1 * * ?")
	public void deleteExpired() {
		// TODO*: when add more admins, will need to add admin_id to each davenfor
		// (unless in different DBs?), and method will check each davenfor according to
		// the admin's properties.		
		System.out.println("Begin deleteExpired().  Amount of davenfors currently: " + davenforRepository.count());

		int waitBeforeDeletion = adminService.getWaitBeforeDeletion(adminId);
		davenforRepository.deleteByExpireAtLessThan(LocalDate.now().minusDays(waitBeforeDeletion));

		System.out.println("End deleteExpired().  Amount of davenfors currently: " + davenforRepository.count());
	}

	// Sends email to davenfor's submitter to let them know the expiring name will
	// be deleted.
	// Fires at 1 a.m. every day
	@Scheduled(cron = "0 0 1 * * ?")
	public void offerExtensionOrDelete() {
		System.out.println("Begin offerExtensionOrDelete().  Davenfors in question:");
		List<Davenfor> expiredDavenfors = davenforRepository.findByExpireAtLessThan(LocalDate.now());
		System.out.println(expiredDavenfors);
		for (Davenfor d : expiredDavenfors) {
			emailSender.offerExtensionOrDelete(d);
		}
		System.out.println("End offerExtensionOrDelete()");
	}

	// Every Thursday at 8 a.m. an email will be sent to Admin with a link to see
	// list and send out, with link to his login page.
	@Scheduled(cron = "0 0 8 * * THU")
	public void remindAdmin() {
		System.out.println("Begin remindAdmin()");
		emailSender.informAdmin(EmailScheme.weeklyAdminReminderSubject, utilities.setWeeklyAdminReminderMessage());
		System.out.println("End remindAdmin()");
	}

	// Every Sunday at 2 a.m. changes category and parasha
	@Scheduled(cron = "0 0 2 * * SUN")
	public void updateNewWeek() {
		System.out.println("Begin updateNewWeek().  Current category: " + adminService.findCurrentCategory() + ", current parasha: " + adminService.findCurrentParasha());
		adminService.updateCurrentCategory();
		adminService.updateParasha();
		System.out.println("End updateNewWeek().  New category: " + adminService.findCurrentCategory() + ", new parasha: " + adminService.findCurrentParasha());
	}

}
