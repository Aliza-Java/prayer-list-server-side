package com.aliza.davening;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.CategoryType;
import com.aliza.davening.repositories.CategoryRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepCategoryTests {

	@Autowired
	private TestEntityManager testEntityManager;

	@Autowired
	CategoryRepository categoryRep;

	@Test
	@Order(1)
	public void noNameExTest() {

		// exception thrown if category is saved without a cname
		Category noName = new Category();
		noName.setCurrent(false);

		assertThrows(DataIntegrityViolationException.class, () -> {
			categoryRep.save(noName);
		});
	}

	@Test
	@Order(2)
	public void notUniqueNameExTest() {
		Category cat1 = new Category();
		cat1.setCname(CategoryType.BANIM);
		categoryRep.save(cat1);

		Category cat2 = new Category();
		cat2.setCname(CategoryType.BANIM);
		assertThrows(DataIntegrityViolationException.class, () -> {
			categoryRep.save(cat2);
		});
	}

	@Test
	@Order(3)
	public void getCurrentTest() {
		assertEquals(Optional.empty(), categoryRep.getCurrent());
		Category c = new Category();
		c.setCname(CategoryType.REFUA);
		c.setCurrent(true);
		categoryRep.save(c);

		Optional<Category> retrieved = categoryRep.getCurrent();
		assertTrue(retrieved.isPresent());
		assertTrue(retrieved.get().isCurrent());
	}

	@Test
	@Order(4)
	public void updateCategoryOrderTest() {
		Category c1 = new Category();
		c1.setCname(CategoryType.REFUA);
		c1.setCatOrder(8);
		categoryRep.save(c1);

		Category c2 = new Category();
		c2.setCname(CategoryType.YESHUAH);
		c2.setCatOrder(10);
		categoryRep.save(c2);

		long id1 = categoryRep.findByCname(CategoryType.REFUA).get().getId();
		long id2 = categoryRep.findByCname(CategoryType.YESHUAH).get().getId();

		categoryRep.updateCategoryOrder(10, id1);
		categoryRep.updateCategoryOrder(8, id2);

		testEntityManager.refresh(categoryRep.findById(id1).get());
		testEntityManager.refresh(categoryRep.findById(id2).get());

		Optional<Category> retrieved1 = categoryRep.findByCname(CategoryType.REFUA);
		Optional<Category> retrieved2 = categoryRep.findByCname(CategoryType.YESHUAH);
		System.out.println(retrieved1);
		System.out.println(retrieved2);

		assertTrue(retrieved1.get().getCatOrder() > retrieved2.get().getCatOrder());
	}

	@Test
	@Order(5)
	public void updateCategoryCurrentTest() {
		Category c1 = new Category();
		c1.setCname(CategoryType.REFUA);
		c1.setCurrent(false);
		categoryRep.save(c1);

		Category c2 = new Category();
		c2.setCname(CategoryType.YESHUAH);
		c2.setCurrent(true);
		categoryRep.save(c2);

		assertTrue(!c1.isCurrent() && c2.isCurrent());

		long id1 = categoryRep.findByCname(CategoryType.REFUA).get().getId();
		long id2 = categoryRep.findByCname(CategoryType.YESHUAH).get().getId();

		categoryRep.updateCategoryCurrent(true, id1);
		categoryRep.updateCategoryCurrent(false, id2);

		testEntityManager.refresh(categoryRep.findById(id1).get());
		testEntityManager.refresh(categoryRep.findById(id2).get());

		Optional<Category> retrieved1 = categoryRep.findByCname(CategoryType.REFUA);
		Optional<Category> retrieved2 = categoryRep.findByCname(CategoryType.YESHUAH);

		assertTrue(retrieved1.get().isCurrent() && !retrieved2.get().isCurrent());
	}

	@Test
	@Order(6)
	public void findAllOrderByIdTest() {
	    EntityManager entityManager = testEntityManager.getEntityManager();

		entityManager.createNativeQuery("INSERT INTO Category (id, cat_order, is_current, cname, update_rate) VALUES (11, 1, true, 'REFUA', 30)").executeUpdate();
		entityManager.createNativeQuery("INSERT INTO Category (id, cat_order, is_current, cname, update_rate) VALUES (17, 2, false, 'YESHUAH', 180)").executeUpdate();
		entityManager.createNativeQuery("INSERT INTO Category (id, cat_order, is_current, cname, update_rate) VALUES (9, 3, false, 'BANIM', 180)").executeUpdate();

		List<Category> orderedCats = categoryRep.findAllOrderById();
		assertEquals("[9, 11, 17]",orderedCats.stream().map(Category::getId).collect(Collectors.toList()).toString());
	}
	
	@Test
	@Order(7)
	public void findByCnameTest()
	{
		Category c1 = new Category();
		c1.setCname(CategoryType.REFUA);
		categoryRep.save(c1);

		Category c2 = new Category();
		c2.setCname(CategoryType.YESHUAH);
		categoryRep.save(c2);
		
		assertEquals("REFUA", categoryRep.findByCname(CategoryType.REFUA).get().getCname().toString());
		assertEquals("YESHUAH", categoryRep.findByCname(CategoryType.YESHUAH).get().getCname().toString());
		assertEquals(Optional.empty(), categoryRep.findByCname(CategoryType.SOLDIERS));
	}

}
