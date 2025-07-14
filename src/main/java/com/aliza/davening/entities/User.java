package com.aliza.davening.entities;

import javax.persistence.Entity;
import javax.validation.constraints.*;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//No ToString in order to override and write one without Davenfors (causes recursive Json)

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Pattern(regexp = "^[a-zA-Z '\\-\\ ]*$", message = "User name must contain only letters and may have spaces in between. ")
	private String name;
	
	@NotBlank(message = "User's email missing. ") // NotBlank includes NotNull option.
	@Pattern(regexp = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,5}))?$", message = "Please provide a valid email for user. ")
	private String email;
	
	@NotBlank(message = "User must have a country. ")
	private String country = "Israel";

	
	@Pattern(regexp = "^[0-9]+$", message = "Whatsapp number can contain only numeric digits.")
	private String whatsapp;

	//String preferred over long - phone can reach 19 digits and also allows dashes in future
	@Pattern(regexp = "^[0-9]+$", message = "Whatsapp number can contain only numeric digits.")
	private String phone;
	
	// Does user want to accept weekly davening list and alerts?
	private boolean active = true;
	
	private String otp;

	// Constructor with only email, for quick creation.
	@NotBlank(message = "User's email missing. ") 
	@Pattern(regexp = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,5}))?$", message = "Please provide a valid email for user. ") 
	public User(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", whatsapp=" + whatsapp + ", phone="
				+ phone + "]";
	}

	public User(User current){
		this.country = current.country;
		this.email = current.email;
		this.whatsapp = current.whatsapp;
		this.active = current.active;
	}
}