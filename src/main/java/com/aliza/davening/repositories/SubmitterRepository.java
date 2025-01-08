package com.aliza.davening.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aliza.davening.entities.Submitter;

public interface SubmitterRepository extends JpaRepository<Submitter, Long> {

	public Optional<Submitter> findByEmail(String email);
}
