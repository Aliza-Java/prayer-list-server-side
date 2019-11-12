package com.aliza.davening.repositories;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davenfor;

@Repository
public interface DavenforRepository extends JpaRepository<Davenfor, Long> {

	// Extend expiration date by adding to it amount of days according to category
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.expireAt=?1 where id=?2")
	public void extendExpiryDate(LocalDate newExpireAt, long davenforId);

	// Changing the lastConfirmed to today's date (sent in as parameter)
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.lastConfirmedAt=?1 where id=?2")
	public void setLastConfirmedAt(LocalDate today, long davenforId);
	
	public List<Davenfor> findByExpireAtLessThan(LocalDate expireAt);
	
	public List<Davenfor> findAllDavenforBySubmitterEmail(String email);
	
	public List<Davenfor> findAllDavenforByCategory(Category category);

}
