package com.aliza.davening.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{

	@Query("select email from Admin where id=?1")
	public String FindAdminEmailById(long id);
	
	@Query("select a from Admin a where a.email=?1")
	Optional<Admin> getAdminByEmail(String email);

	@Query("select waitBeforeDeletion from Admin a where id=?1")
	public int getWaitBeforeDeletion(long id);	
}
