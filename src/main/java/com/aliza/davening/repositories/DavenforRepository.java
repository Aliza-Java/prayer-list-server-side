package com.aliza.davening.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.entities.CategoryName;
import com.aliza.davening.entities.Davenfor;

@Repository
public interface DavenforRepository extends JpaRepository<Davenfor, Long> {

	// Extend expiration date by adding to it amount of days according to category,
	// as well as updating the confirmed and updated fields.
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.expireAt=?2, lastConfirmedAt=?3, updatedAt=?3 where id=?1")
	public void extendExpiryDate(long davenforId, LocalDate extendedDate, LocalDate today);

	// Changing the lastConfirmed to today's date (sent in as parameter)
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.lastConfirmedAt=?1 where id=?2")
	public void setLastConfirmedAt(LocalDate today, long davenforId);

	public List<Davenfor> findByExpireAtLessThan(LocalDate expireAt);

	@Transactional
	@Modifying
	public void deleteByExpireAtLessThan(LocalDate expireAt);

	public List<Davenfor> findAllDavenforBySubmitterEmail(String email);

	List<Davenfor> findAllDavenforByCategory(String category);
}
