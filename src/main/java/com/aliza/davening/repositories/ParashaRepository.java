package com.aliza.davening.repositories;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Parasha;

@Repository
public interface ParashaRepository extends JpaRepository<Parasha, Long> {

	@Query("select p from Parasha p where p.current=1")
	public Optional<Parasha> findCurrent();

	@Transactional
	@Modifying
	@Query("update Parasha p set p.current=?1 where id=?2")
	public void updateParashaCurrent(boolean current, long id);
}
