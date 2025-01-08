package com.aliza.davening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aliza.davening.entities.Submitter;
import com.aliza.davening.repositories.SubmitterRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepSubmitterTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	SubmitterRepository submitterRep;

	@Test
	public void findByEmailTest() {

		Submitter submitter = new Submitter();
		submitter.setEmail("sub@gmail.com");
		submitterRep.save(submitter);

		Optional<Submitter> retrieved = submitterRep.findByEmail("sub@gmail.com");
		assertTrue(retrieved.isPresent());
		assertEquals(retrieved.get(), submitter);

		assertEquals(Optional.empty(), submitterRep.findByEmail("none.existing@gmail.com"));
	}
}
