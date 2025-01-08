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

import com.aliza.davening.entities.Davener;
import com.aliza.davening.repositories.DavenerRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepDavenerTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	DavenerRepository davenerRep;

	@Test
	@Order(1)
	public void getAllDavenerEmailsTest() {

		Davener davener1 = new Davener(1, "Israel", "davener1@gmail.com", null, true);
		Davener davener2 = new Davener(2, "Israel", "davener2@gmail.com", null, false);
		Davener davener3 = new Davener(3, "Israel", "davener3@gmail.com", null, true);
		davenerRep.save(davener1);
		davenerRep.save(davener2);
		davenerRep.save(davener3);

		List<String> retrieved = davenerRep.getAllDavenersEmails();
		assertEquals(2, retrieved.size());
		assertEquals("davener1@gmail.com", retrieved.get(0));
		assertEquals("davener3@gmail.com", retrieved.get(1));
	}

	@Test
	@Order(2)
	public void disactivateDavenerTest() {
		Davener davener = new Davener();
		davener.setEmail("please.disactivate@gmail.com");
		davener.setActive(true);
		davenerRep.save(davener);

		Davener davener2 = new Davener();
		davener2.setEmail("dont.touch@gmail.com");
		davener2.setActive(true);
		davenerRep.save(davener2);

		davenerRep.disactivateDavener("please.disactivate@gmail.com");

		testEntityManager.refresh(davener);
		testEntityManager.refresh(davener2);

		Optional<Davener> retrieved = davenerRep.findByEmail("please.disactivate@gmail.com");
		assertTrue(retrieved.isPresent());
		assertFalse(retrieved.get().isActive());

		Optional<Davener> retrieved2 = davenerRep.findByEmail("dont.touch@gmail.com");
		assertTrue(retrieved2.isPresent());
		assertTrue(retrieved2.get().isActive());
	}

	@Test
	@Order(3)
	public void activateDavenerTest() {
		Davener davener = new Davener();
		davener.setEmail("please.activate@gmail.com");
		davener.setActive(false);
		davenerRep.save(davener);

		Davener davener2 = new Davener();
		davener2.setEmail("dont.touch@gmail.com");
		davener2.setActive(false);
		davenerRep.save(davener2);

		davenerRep.activateDavener("please.activate@gmail.com");

		testEntityManager.refresh(davener);
		testEntityManager.refresh(davener2);

		Optional<Davener> retrieved = davenerRep.findByEmail("please.activate@gmail.com");
		assertTrue(retrieved.isPresent());
		assertTrue(retrieved.get().isActive());

		Optional<Davener> retrieved2 = davenerRep.findByEmail("dont.touch@gmail.com");
		assertTrue(retrieved2.isPresent());
		assertFalse(retrieved2.get().isActive());
	}

	@Test
	@Order(4)
	public void findByEmailTest() {
		Davener davener1 = new Davener(1, "Israel", "davener1@gmail.com", null, true);
		Davener davener2 = new Davener(2, "Morocco", "davener2@gmail.com", null, false);
		Davener davener3 = new Davener(3, "Japan", "davener3@gmail.com", null, true);
		davenerRep.save(davener1);
		davenerRep.save(davener2);
		davenerRep.save(davener3);

		Optional<Davener> retrieved = davenerRep.findByEmail("davener2@gmail.com");
		assertTrue(retrieved.isPresent());
		assertEquals("Morocco", retrieved.get().getCountry());

		assertEquals(Optional.empty(), davenerRep.findByEmail("none.existing@gmail.com"));
	}

	@Test
	@Order(5)
	public void findAllTest() {
		Davener davener1 = new Davener(1, "Israel", "davener1@gmail.com", null, true);
		Davener davener2 = new Davener(2, "Morocco", "davener2@gmail.com", null, false);
		Davener davener3 = new Davener(3, "Japan", "davener3@gmail.com", null, true);
		davenerRep.save(davener1);
		davenerRep.save(davener2);
		davenerRep.save(davener3);

		List<Davener> retrieved = davenerRep.findAll();
		assertEquals(3, retrieved.size());
		assertEquals("davener3@gmail.com", retrieved.get(2).getEmail());
	}
}
