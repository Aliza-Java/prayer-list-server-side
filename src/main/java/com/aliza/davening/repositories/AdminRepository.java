package com.aliza.davening.repositories;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
	
	@Modifying
	@Transactional
	@Query("update Admin set email=?2, newNamePrompt=?3, waitBeforeDeletion=4 where id=?1")
	public void updateSettings(long id, String email, boolean prompt, int wait);
		
	Boolean existsByEmail(String email);

}
