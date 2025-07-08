package com.aliza.davening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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

import com.aliza.davening.entities.User;
import com.aliza.davening.repositories.UserRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepUserTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	UserRepository userRep;

	@Test
	@Order(1)
	public void getAllUserEmailsTest() {

		User user1 = new User(1, null, "user1@gmail.com", "Israel", null, null, true, "");
		User user2 = new User(2, null, "user2@gmail.com", "Israel", null, null, false, "");
		User user3 = new User(3, null, "user3@gmail.com", "Israel", null, null, true, "");

		userRep.save(user1);
		userRep.save(user2);
		userRep.save(user3);

		List<String> retrieved = userRep.getAllUsersEmails();
		assertEquals(2, retrieved.size());
		assertEquals("user1@gmail.com", retrieved.get(0));
		assertEquals("user3@gmail.com", retrieved.get(1));
	}

	@Test
	@Order(2)
	public void disactivateUserTest() {
		User user = new User();
		user.setEmail("please.disactivate@gmail.com");
		user.setActive(true);
		userRep.save(user);

		User user2 = new User();
		user2.setEmail("dont.touch@gmail.com");
		user2.setActive(true);
		userRep.save(user2);

		userRep.disactivateUser("please.disactivate@gmail.com");

		testEntityManager.refresh(user);
		testEntityManager.refresh(user2);

		Optional<User> retrieved = userRep.findByEmail("please.disactivate@gmail.com");
		assertTrue(retrieved.isPresent());
		assertFalse(retrieved.get().isActive());

		Optional<User> retrieved2 = userRep.findByEmail("dont.touch@gmail.com");
		assertTrue(retrieved2.isPresent());
		assertTrue(retrieved2.get().isActive());
	}

	@Test
	@Order(3)
	public void activateUserTest() {
		User user = new User();
		user.setEmail("please.activate@gmail.com");
		user.setActive(false);
		userRep.save(user);

		User user2 = new User();
		user2.setEmail("dont.touch@gmail.com");
		user2.setActive(false);
		userRep.save(user2);

		userRep.activateUser("please.activate@gmail.com");

		testEntityManager.refresh(user);
		testEntityManager.refresh(user2);

		Optional<User> retrieved = userRep.findByEmail("please.activate@gmail.com");
		assertTrue(retrieved.isPresent());
		assertTrue(retrieved.get().isActive());

		Optional<User> retrieved2 = userRep.findByEmail("dont.touch@gmail.com");
		assertTrue(retrieved2.isPresent());
		assertFalse(retrieved2.get().isActive());
	}

	@Test
	@Order(4)
	public void findByEmailTest() {
		User user1 = new User(1, null, "user1@gmail.com", "Israel", null, null, false, "");
		User user2 = new User(2, null, "user2@gmail.com", "Morocco", null, null, false, "");
		User user3 = new User(3, null, "user3@gmail.com", "Japan", null, null, true, "");

		userRep.save(user1);
		userRep.save(user2);
		userRep.save(user3);

		Optional<User> retrieved = userRep.findByEmail("user2@gmail.com");
		assertTrue(retrieved.isPresent());
		assertEquals("Morocco", retrieved.get().getCountry());

		assertEquals(Optional.empty(), userRep.findByEmail("none.existing@gmail.com"));
	}

	@Test
	@Order(5)
	public void findAllTest() {
		User user1 = new User(1, null, "user1@gmail.com", "Israel", null, null, true, "");
		User user2 = new User(2, null, "user2@gmail.com", "Morocco", null, null, false, "");
		User user3 = new User(3, null, "user3@gmail.com", "Japan", null, null, true, "");

		userRep.save(user1);
		userRep.save(user2);
		userRep.save(user3);

		List<User> retrieved = userRep.findAll();
		assertEquals(3, retrieved.size());
		assertEquals("user3@gmail.com", retrieved.get(2).getEmail());
	}

}
