package com.aliza.davening.repositories;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Modifying
	@Transactional
	@Query("update Category set catOrder=?1 where id=?2")
	public void updateCategoryOrder(int newOrder, long categoryId);

	@Query("select c from Category c where c.isCurrent=true")
	public Category getCurrent();
	
}
