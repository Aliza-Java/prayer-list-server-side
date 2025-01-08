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

import com.aliza.davening.entities.Parasha;
import com.aliza.davening.repositories.ParashaRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepParashaTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	ParashaRepository parashaRep;

	@Test
	public void findCurrentTest() {

		Parasha parasha1 = new Parasha(1, "Bereshit", "בראשית", false);
		Parasha parasha2 = new Parasha(2, "Noach", "נח", true);
		Parasha parasha3 = new Parasha(3, "Lech Lecha", "לך-לך", false);
		parashaRep.save(parasha1);
		parashaRep.save(parasha2);
		parashaRep.save(parasha3);

		Optional<Parasha> retrieved = parashaRep.findCurrent();
		assertTrue(retrieved.isPresent());
		assertEquals("Noach", retrieved.get().getEnglishName());
		assertEquals("נח", retrieved.get().getHebrewName());

		parashaRep.delete(parasha2);

		assertEquals(Optional.empty(), parashaRep.findCurrent());
	}
}
