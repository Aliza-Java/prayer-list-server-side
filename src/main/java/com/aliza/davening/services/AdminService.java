package com.aliza.davening.services;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliza.davening.entities.Admin;
import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.Davener;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenerRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;

import exceptions.ObjectNotFoundException;

@Service("adminService")
public class AdminService {

	@Autowired
	DavenerRepository davenerRepository;

	@Autowired
	DavenforRepository davenforRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	SubmitterRepository submitterRepository;
	
	@Autowired
	AdminRepository adminRepository;

	public Admin login(String email, String password) {
		Optional<Admin> optionalAdmin = adminRepository.getAdminByEmailAndPassword(email, password);
		if(!optionalAdmin.isPresent()) {
			return null;
		}
		return optionalAdmin.get();
	}
	
	public Admin updateAdmin(Admin admin) {
		
		
	}

	
	
	
	
	
	
	
	
	/*
	 * public void createDavener(Davener davener) { davenerRepository.save(davener);
	 * }
	 * 
	 * public Category getCategory(long id) throws ObjectNotFoundException {
	 * Optional<Category> optionalCategory = categoryRepository.findById(id); if
	 * (!optionalCategory.isPresent()) { throw new
	 * ObjectNotFoundException("Category with id " + id); } return
	 * optionalCategory.get(); }
	 * 
	 * public Davener getDavener(long id) throws ObjectNotFoundException {
	 * Optional<Davener> optionalDavener = davenerRepository.findById(id); if
	 * (!optionalDavener.isPresent()) { throw new
	 * ObjectNotFoundException("Davener with id " + id); } return
	 * optionalDavener.get(); }
	 * 
	 * public void createDavenfor(Davenfor davenfor) {
	 * davenforRepository.save(davenfor); }
	 * 
	 * public long createCategory(Category category) {
	 * categoryRepository.save(category); return category.getId(); }
	 * 
	 * public long createSubmitter(Submitter submitter) {
	 * submitterRepository.save(submitter); return submitter.getId(); }
	 * 
	 * public long createAdmin(Admin admin) { adminRepository.save(admin); return
	 * admin.getId(); }
	 * 
	 * public void deleteSubmitter(long id) { submitterRepository.deleteById(id); }
	 * 
	 * public Submitter getSubmitter(long id) throws ObjectNotFoundException {
	 * Optional<Submitter> optionalSubmitter = submitterRepository.findById(id); if
	 * (!optionalSubmitter.isPresent()) { throw new
	 * ObjectNotFoundException("Submitter with id " + id); } return
	 * optionalSubmitter.get(); }
	 * 
	 * public void deleteDavenfor(long id) { davenforRepository.deleteById(id); }
	 * 
	 * @Transactional public Davenfor getDavenfor(long id) throws
	 * ObjectNotFoundException { Optional<Davenfor> optionalDavenfor =
	 * davenforRepository.findById(id); if (!optionalDavenfor.isPresent()) { throw
	 * new ObjectNotFoundException("Davenfor with id " + id); } return
	 * optionalDavenfor.get(); }
	 * 
	 * public void updateSubmitterName(String newName) { Optional<Submitter>
	 * optionalSubmitter = submitterRepository.findById((long) 7); if
	 * (optionalSubmitter.isPresent()) { Submitter updatedSubmitter =
	 * optionalSubmitter.get(); updatedSubmitter.setName(newName);
	 * submitterRepository.save(updatedSubmitter); }
	 * 
	 * }
	 * 
	 * public void saveUpdatedDavenfor() { Optional<Davenfor> optionalDavenfor =
	 * davenforRepository.findById((long) 16); if (optionalDavenfor.isPresent()) {
	 * Davenfor davenforToUpdate = optionalDavenfor.get();
	 * davenforToUpdate.setNameEnglish("Davenfor English name");
	 * davenforRepository.save(davenforToUpdate); }
	 * 
	 * }
	 * 
	 * public void deleteCategory(long id) { categoryRepository.deleteById(id); }
	 * 
	 * public List<Davenfor> getAllDavenfors() { return
	 * davenforRepository.findAll(); }
	 * 
	 * public void saveUpdatedCategory() { Optional<Category> optionalCategory =
	 * categoryRepository.findById((long) 8); if (optionalCategory.isPresent()) {
	 * Category categoryToUpdate = optionalCategory.get();
	 * categoryToUpdate.setUpdateRate(10000);
	 * categoryRepository.save(categoryToUpdate); } }
	 */
	
}
