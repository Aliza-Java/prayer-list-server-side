package com.aliza.davening.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotBlank(message = "Admin's email missing. ")
	@Pattern(regexp = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,5}))?$", message = "Please provide a valid email for the admin. ")
	private String email;

	@NotBlank(message = "Admin password empty. ")
	@Size(min = 8, message = "Password must be at least 8 characters long.")
	private String password;

	// Does the groupAdmin want to be prompted when new names are added? default
	// upon creation is false.
	private boolean newNamePrompt;

	// how many days to wait after submitter was warned name is expired default 7
	private int waitBeforeDeletion;

}
