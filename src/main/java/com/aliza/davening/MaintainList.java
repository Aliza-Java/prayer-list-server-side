package com.aliza.davening;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.ParashaRepository;
import com.aliza.davening.services.AdminService;
import com.aliza.davening.services.EmailSender;

@Component
public class MaintainList {

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	AdminService adminService;

	@Autowired
	EmailSender emailSender;

	@Autowired
	ParashaRepository parashaRepository;
	
	@Value("${admin.id}")
	private long adminId=1;

	// Deletes davenfors which have expired, even past the grace period of
	// wait.before.deletion
	// Fires at 1 a.m. every day
	@Scheduled(cron = "0 0 1 * * ?")
	public void deleteExpired() {
		// TODO: when add more admins, will need to add admin_id to each davenfor
		// (unless in different DBs?), and method will check each davenfor according to
		// the admin's properties.
		
		int waitBeforeDeletion = adminService.getWaitBeforeDeletion(adminId);

		davenforRepository.deleteByExpireAtLessThan(LocalDate.now().minusDays(waitBeforeDeletion));
	}

	// Sends email to davenfor's submitter to let them know the expiring name will
	// be deleted.
	// Fires at 1 a.m. every day
	@Scheduled(cron = "0 0 1 * * ?")
	public void offerExtensionOrDelete() throws EmailException {
		List<Davenfor> expiredDavenfors = davenforRepository.findByExpireAtLessThan(LocalDate.now());
		System.out.println(expiredDavenfors);
		for (Davenfor d : expiredDavenfors) {
		//	emailSender.offerExtensionOrDelete(d);
		}
	}

	// Every Thursday at 8 a.m. an email will be sent to Admin with a link to see
	// list and send out, with link to his login page.
	@Scheduled(cron = "0 0 8 * * THU")
	public void remindAdmin() throws EmailException {
	//	emailSender.informAdmin(EmailScheme.getWeeklyAdminReminderSubject(), Utilities.setWeeklyAdminReminderMessage());
	}

	// Every Sunday at 2 a.m. changes category
	@Scheduled(cron = "0 0 2 * * SUN")
	public void updateCurrentCategory() {
		adminService.updateCurrentCategory();
	}

}
