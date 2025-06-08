package com.aliza.davening.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aliza.davening.entities.Davenfor;

@Repository
public interface DavenforRepository extends JpaRepository<Davenfor, Long> {

//	// Extend expiration date by adding to it amount of days according to category,
//	// as well as updating the confirmed and updated fields.
//	@Transactional
//	@Modifying
//	@Query("update Davenfor d set confirmedAt=?2, updatedAt=?2 where id=?1")
//	public void extendExpiryDate(long davenforId, LocalDateTime today);

	// Changing the lastConfirmed to today's date (sent in as parameter)
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.confirmedAt=?1 where id=?2")
	public void setConfirmedAt(LocalDateTime today, long davenforId);
	
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.deletedAt=null where id=?1")
	public void reviveDavenfor(long davenforId);

	// public List<Davenfor> findByExpireAtLessThan(LocalDateTime expireAt); //this
	// doesn't include the date itself

//	@Transactional
//	@Modifying
//	public void deleteByExpireAtLessThan(LocalDateTime expireAt);

	public List<Davenfor> findAllDavenforByUserEmail(String email);

	List<Davenfor> findAllByCategoryAndConfirmedAtIsNull(String category);

	List<Davenfor> findAllDavenforByCategory(String category);

	// clearing all past confirmations, for users to confirm relevant davenfors for
	// this week
	@Transactional
	@Modifying
	@Query("update Davenfor d set d.confirmedAt = null where category=?1")
	public void clearConfirmedAt(String category);

	@Modifying
	@Query("UPDATE Davenfor d SET d.deletedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
	void softDeleteById(@Param("id") Long id);
	
	@Query(value = "SELECT * FROM davenfor WHERE id = :id", nativeQuery = true)
	Optional<Davenfor> findByIdIncludingDeleted(@Param("id") Long id);
}
