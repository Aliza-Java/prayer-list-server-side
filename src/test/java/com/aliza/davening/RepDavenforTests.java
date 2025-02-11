package com.aliza.davening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
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
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepDavenforTests {

	@Autowired
	TestEntityManager testEntityManager;

	@Autowired
	DavenforRepository davenforRep;

	@Autowired
	CategoryRepository categoryRep;

	@Test
	@Order(1)
	public void extendExpiryDateTest() {

		Davenfor dfRefua = new Davenfor(1L, "user1@gmail.com", "refua", "אברהם בן שרה", "Avraham ben Sara", null, null,
				true, null, null, null, null, null);
		LocalDate today = LocalDate.now();
		LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
		LocalDate nextWeek = LocalDate.now().plusDays(7);
		dfRefua.setExpireAt(today);
		dfRefua.setUpdatedAt(fiveDaysAgo);
		dfRefua.setLastConfirmedAt(fiveDaysAgo);
		davenforRep.save(dfRefua);

		assertTrue(dfRefua.getUpdatedAt().isBefore(today));

		davenforRep.extendExpiryDate(dfRefua.getId(), nextWeek, today);
		testEntityManager.refresh(davenforRep.findById(dfRefua.getId()).get());

		Optional<Davenfor> retrieved = davenforRep.findById(dfRefua.getId());
		assertTrue(retrieved.isPresent());
		assertTrue(retrieved.get().getExpireAt().isAfter(today));
		assertEquals(today, retrieved.get().getUpdatedAt());
		assertEquals(today, retrieved.get().getLastConfirmedAt());
	}

	@Test
	@Order(2)
	public void setLastConfirmedAtTest() {
		Davenfor dfYeshuah = new Davenfor(2L, "user1@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara", null, null,
				true, null, null, null, null, null);
		dfYeshuah.setLastConfirmedAt(LocalDate.now().minusDays(3));
		davenforRep.save(dfYeshuah);

		Long savedId = dfYeshuah.getId();
		assertTrue(dfYeshuah.getLastConfirmedAt().isBefore(LocalDate.now()));
		davenforRep.setLastConfirmedAt(LocalDate.now(), savedId);
		testEntityManager.refresh(davenforRep.findById(savedId).get());

		Optional<Davenfor> retrieved = davenforRep.findById(dfYeshuah.getId());
		assertTrue(retrieved.isPresent());
		assertEquals(LocalDate.now(), retrieved.get().getLastConfirmedAt());
	}

	@Test
	@Order(3)
	public void findByExpireAtLessThanTest() {

		Davenfor df1 = new Davenfor(3L, "early@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df2 = new Davenfor(4L, "early@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
				"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df3 = new Davenfor(5L, "late@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira", null, null,
				true, null, null, null, null, null);
		df1.setExpireAt(LocalDate.now().minusDays(2));
		df2.setExpireAt(LocalDate.now().minusDays(4));
		df3.setExpireAt(LocalDate.now().plusDays(3));
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);

		List<Davenfor> retrieved = davenforRep.findByExpireAtLessThan(LocalDate.now());
		assertEquals(2, retrieved.size());
		assertEquals("early@gmail.com", retrieved.get(0).getUserEmail());
		assertEquals("early@gmail.com", retrieved.get(1).getUserEmail());
	}

	@Test
	@Order(4)
	public void deleteByExpireAtLessThanTest() {
		Davenfor df1 = new Davenfor(6L, "early@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df2 = new Davenfor(7L, "early@gmail.com", "Banim", "אברהם בן שרה", "Avraham ben Sara",
				"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df3 = new Davenfor(8L, "late@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira", null, null,
				true, null, null, null, null, null);
		df1.setExpireAt(LocalDate.now().minusDays(2));
		df2.setExpireAt(LocalDate.now().minusDays(4));
		df3.setExpireAt(LocalDate.now().plusDays(3));
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);

		davenforRep.deleteByExpireAtLessThan(LocalDate.now());
		List<Davenfor> remaining = davenforRep.findAll();
		assertEquals(1, remaining.size());
		assertEquals("late@gmail.com", remaining.get(0).getUserEmail());
	}

	@Test
	@Order(5)
	public void findAllDavenforBySubmitterEmailTest() {
		Davenfor df1 = new Davenfor(9L, "user1@gmail.com", "Yeshuah", "אברהם בן שרה", "Avraham ben Sara", null, null,
				true, null, null, null, null, null);
		Davenfor df2 = new Davenfor(10L, "user1@gmail.com", "Yeshuah", "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df3 = new Davenfor(11L, "user2@gmail.com", "Yeshuah", "אברהם בן שרה", "Avraham ben Sara",
				"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df4 = new Davenfor(12L, "user1@gmail.com", "Yeshuah", "עמרם בן שירה", "Amram ben Shira", null, null,
				true, null, null, null, null, null);
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);
		davenforRep.save(df4);

		List<Davenfor> retrieved = davenforRep.findAllDavenforByUserEmail("user1@gmail.com");
		assertEquals(3, retrieved.size());
		assertEquals(df1.getId(), retrieved.get(0).getId());
		assertEquals(df2.getId(), retrieved.get(1).getId());
		assertEquals(df4.getId(), retrieved.get(2).getId());
	}

	@Test
	@Order(6)
	public void findAllDavenforByCategoryTest() {
		Davenfor df1 = new Davenfor(13L, "user1@gmail.com", "Yeshuah", "אברהם בן שרה", "Avraham ben Sara", null, null,
				true, null, null, null, null, null);
		Davenfor df2 = new Davenfor(14L, "user2@gmail.com", "Soldiers", "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df3 = new Davenfor(15L, "user3@gmail.com", "Yeshuah", "אברהם בן שרה", "Avraham ben Sara",
				"יהודית בת מרים", "Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df4 = new Davenfor(16L, "user4@gmail.com", "Soldiers", "עמרם בן שירה", "Amram ben Shira", null, null,
				true, null, null, null, null, null);
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);
		davenforRep.save(df4);

		List<Davenfor> retrieved = davenforRep.findAllDavenforByCategory("Soldiers");
		assertEquals(2, retrieved.size());
		assertEquals(df2.getId(), retrieved.get(0).getId());
		assertEquals(df4.getId(), retrieved.get(1).getId());
	}
}
