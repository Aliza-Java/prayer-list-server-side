package com.aliza.davening;

import static com.aliza.davening.entities.CategoryName.BANIM;
import static com.aliza.davening.entities.CategoryName.REFUA;
import static com.aliza.davening.entities.CategoryName.SOLDIERS;
import static com.aliza.davening.entities.CategoryName.YESHUAH;
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

import com.aliza.davening.entities.Category;
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

		Category refua = new Category(REFUA, true, 180, 1);
		categoryRep.save(refua);

		Davenfor dfRefua = new Davenfor(1, "sub1@gmail.com", refua, "אברהם בן שרה", "Avraham ben Sara", null, null,
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
		Category yeshuah = new Category(YESHUAH, false, 180, 5);
		categoryRep.save(yeshuah);

		Davenfor dfYeshuah = new Davenfor(2, "sub1@gmail.com", yeshuah, "משה בן שרה", "Moshe ben Sara", null, null,
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
		Category yeshuah = new Category(YESHUAH, false, 180, 5);
		Category banim = new Category(BANIM, false, 50, 3);
		categoryRep.save(yeshuah);
		categoryRep.save(banim);

		Davenfor df1 = new Davenfor(3, "early@gmail.com", yeshuah, "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df2 = new Davenfor(4, "early@gmail.com", banim, "אברהם בן שרה", "Avraham ben Sara", "יהודית בת מרים",
				"Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df3 = new Davenfor(5, "late@gmail.com", yeshuah, "עמרם בן שירה", "Amram ben Shira", null, null, true,
				null, null, null, null, null);
		df1.setExpireAt(LocalDate.now().minusDays(2));
		df2.setExpireAt(LocalDate.now().minusDays(4));
		df3.setExpireAt(LocalDate.now().plusDays(3));
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);

		List<Davenfor> retrieved = davenforRep.findByExpireAtLessThan(LocalDate.now());
		assertEquals(2, retrieved.size());
		assertEquals("early@gmail.com", retrieved.get(0).getSubmitterEmail());
		assertEquals("early@gmail.com", retrieved.get(1).getSubmitterEmail());
	}

	@Test
	@Order(4)
	public void deleteByExpireAtLessThanTest() {
		Category yeshuah = new Category(YESHUAH, false, 180, 5);
		Category banim = new Category(BANIM, false, 50, 3);
		categoryRep.save(yeshuah);
		categoryRep.save(banim);

		Davenfor df1 = new Davenfor(6, "early@gmail.com", yeshuah, "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df2 = new Davenfor(7, "early@gmail.com", banim, "אברהם בן שרה", "Avraham ben Sara", "יהודית בת מרים",
				"Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df3 = new Davenfor(8, "late@gmail.com", yeshuah, "עמרם בן שירה", "Amram ben Shira", null, null, true,
				null, null, null, null, null);
		df1.setExpireAt(LocalDate.now().minusDays(2));
		df2.setExpireAt(LocalDate.now().minusDays(4));
		df3.setExpireAt(LocalDate.now().plusDays(3));
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);

		davenforRep.deleteByExpireAtLessThan(LocalDate.now());
		List<Davenfor> remaining = davenforRep.findAll();
		assertEquals(1, remaining.size());
		assertEquals("late@gmail.com", remaining.get(0).getSubmitterEmail());
	}

	@Test
	@Order(5)
	public void findAllDavenforBySubmitterEmailTest() {
		Category yeshuah = new Category(YESHUAH, false, 180, 5);
		categoryRep.save(yeshuah);

		Davenfor df1 = new Davenfor(9, "sub1@gmail.com", yeshuah, "אברהם בן שרה", "Avraham ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df2 = new Davenfor(10, "sub1@gmail.com", yeshuah, "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df3 = new Davenfor(11, "sub2@gmail.com", yeshuah, "אברהם בן שרה", "Avraham ben Sara", "יהודית בת מרים",
				"Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df4 = new Davenfor(12, "sub1@gmail.com", yeshuah, "עמרם בן שירה", "Amram ben Shira", null, null, true,
				null, null, null, null, null);
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);
		davenforRep.save(df4);

		List<Davenfor> retrieved = davenforRep.findAllDavenforBySubmitterEmail("sub1@gmail.com");
		assertEquals(3, retrieved.size());
		assertEquals(df1.getId(), retrieved.get(0).getId());
		assertEquals(df2.getId(), retrieved.get(1).getId());
		assertEquals(df4.getId(), retrieved.get(2).getId());
	}

	@Test
	@Order(6)
	public void findAllDavenforByCategoryTest() {
		Category yeshuah = new Category(YESHUAH, false, 180, 5);
		Category soldiers = new Category(SOLDIERS, false, 180, 5);
		categoryRep.save(yeshuah);
		categoryRep.save(soldiers);

		Davenfor df1 = new Davenfor(13, "sub1@gmail.com", yeshuah, "אברהם בן שרה", "Avraham ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df2 = new Davenfor(14, "sub2@gmail.com", soldiers, "משה בן שרה", "Moshe ben Sara", null, null, true,
				null, null, null, null, null);
		Davenfor df3 = new Davenfor(15, "sub3@gmail.com", yeshuah, "אברהם בן שרה", "Avraham ben Sara", "יהודית בת מרים",
				"Yehudit bat Miriam", true, null, null, null, null, null);
		Davenfor df4 = new Davenfor(16, "sub4@gmail.com", soldiers, "עמרם בן שירה", "Amram ben Shira", null, null, true,
				null, null, null, null, null);
		davenforRep.save(df1);
		davenforRep.save(df2);
		davenforRep.save(df3);
		davenforRep.save(df4);

		List<Davenfor> retrieved = davenforRep.findAllDavenforByCategory(SOLDIERS);
		assertEquals(2, retrieved.size());
		assertEquals(df2.getId(), retrieved.get(0).getId());
		assertEquals(df4.getId(), retrieved.get(1).getId());
	}
}
