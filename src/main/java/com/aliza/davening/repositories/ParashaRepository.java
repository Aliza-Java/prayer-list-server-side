package com.aliza.davening.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Parasha;

@Repository
public interface ParashaRepository extends JpaRepository<Parasha, Long> {
	
	@Query("select p from Parasha p where p.current=1")
	public Parasha findCurrent();
	
	}

