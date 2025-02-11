package com.aliza.davening.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.davening.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	 @Query("select u.email from User u where u.active=true")
	   List<String> getAllUsersEmails();
	 
	 @Modifying
	 @Transactional
	 @Query("update User u set u.active=false where email=?1")
	 public void disactivateUser(String email);
	 
	 @Modifying
	 @Transactional
	 @Query("update User u set u.active=true where email=?1")
	 public void activateUser(String email);
	 
	 Optional<User> findByEmail(String email);
	 
	 List<User> findAll();
}
