package com.aliza.davening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.repositories.AdminRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepAdminTests {

	@Autowired
	TestEntityManager testEntityManager;
	
	@Autowired
	AdminRepository adminRep;

	@Test
	@Order(1)
	public void findAmdinEmailByIdTest() {

		Admin admin = new Admin();
		admin.setEmail("admin1@gmail.com");
		adminRep.save(admin);
		assertEquals("admin1@gmail.com", adminRep.FindAdminEmailById(admin.getId()));

	}

	@Test
	@Order(2)
	public void getAdminByEmailTest() {
		Admin admin = new Admin();
		admin.setEmail("admin2@gmail.com");
		adminRep.save(admin);

		assertEquals(Optional.empty(), adminRep.getAdminByEmail("admin1@gmail.com"));
		assertEquals(admin, adminRep.getAdminByEmail("admin2@gmail.com").get());
	}

	@Test
	@Order(3)
	public void getWaitBeforeDeletionTest() {
		Admin admin = new Admin();
		admin.setWaitBeforeDeletion(45);
		adminRep.save(admin);

		assertEquals(45, adminRep.getWaitBeforeDeletion(admin.getId()));
	}

	@Test
	@Order(4)
	public void updateSettingsTest() {
		Admin admin = new Admin();
		admin.setEmail("old@gmail.com");
		admin.setNewNamePrompt(false);
		admin.setWaitBeforeDeletion(1);
		adminRep.save(admin);
		
		long id = admin.getId();
		
		assertEquals("old@gmail.com", adminRep.findById(id).get().getEmail());
		assertFalse(adminRep.findById(id).get().isNewNamePrompt());
		assertEquals(1, adminRep.findById(id).get().getWaitBeforeDeletion());

		adminRep.updateSettings(id, "new@gmail.com", true, 2);
		
		testEntityManager.refresh(adminRep.findById(id).get());

		Admin retrieved = adminRep.findById(id).get();
		assertEquals("new@gmail.com", retrieved.getEmail());
		assertTrue(retrieved.isNewNamePrompt());
		assertEquals(2, retrieved.getWaitBeforeDeletion());
	}

	@Test
	@Order(5)
	public void existsByEmailTest() {
		Admin admin = new Admin();
		admin.setEmail("admin3@gmail.com");
		adminRep.save(admin);

		assertTrue(adminRep.existsByEmail("admin3@gmail.com"));
		assertFalse(adminRep.existsByEmail("noneExistent@gmail.com"));
	}
}
