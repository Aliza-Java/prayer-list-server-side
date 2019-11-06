package com.aliza.davening.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{

}
