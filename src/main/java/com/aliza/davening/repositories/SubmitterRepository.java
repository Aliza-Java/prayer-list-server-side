package com.aliza.davening.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Submitter;

@Repository
public interface SubmitterRepository extends JpaRepository<Submitter, Long> {

	public Submitter findByEmail(String email);

}
