package com.aliza.davening.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aliza.davening.entities.Submitter;

public interface SubmitterRepository extends JpaRepository<Submitter, Long>{

	public Submitter findByEmail(String email);

}
