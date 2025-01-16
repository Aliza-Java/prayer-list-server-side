package com.aliza.davening.repositories;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{

	//tested
	@Query("select email from Admin where id=?1")
	public String FindAdminEmailById(long id);
	
	//tested
	@Query("select a from Admin a where a.email=?1")
	Optional<Admin> getAdminByEmail(String email);

	//tested
	@Query("select waitBeforeDeletion from Admin a where id=?1")
	public int getWaitBeforeDeletion(long id);	
	
	//tested
	@Modifying
	@Transactional
	@Query("update Admin set email=?2, newNamePrompt=?3, waitBeforeDeletion=?4 where id=?1")
	public void updateSettings(long id, String email, boolean prompt, int wait);
		
	
	public Boolean existsByEmail(String email);

	//TODO - add test for this
	public Optional<Admin> findByEmail(String email);
}
