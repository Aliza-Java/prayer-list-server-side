package com.aliza.davening.repositories;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.Davener;

@Repository
public interface DavenerRepository extends JpaRepository<Davener, Long> {
	
	 @Query("select d.email from Davener d where d.active=true")
	   List<String> getAllDavenersEmails();
	 
	 @Modifying
	 @Transactional
	 @Query("update Davener d set d.active=false where id=?1")
	 public void disactivateDavener(long id);
	 
	 Davener findByEmail(String email);

}
