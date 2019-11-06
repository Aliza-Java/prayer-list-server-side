package com.aliza.davening.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
public class Davener {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@NotNull(message = "Davener must have a country. ")
	@NotBlank(message = "Davener country cannot be blank. ")
	private String country="Israel";
	
	@NotNull(message = "Davener must have an associated email. ")
	@NotEmpty
	@NotBlank
	@Pattern(regexp="^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,5}))?$", message="Davener email seems to be invalid.")
	private String email;
	
	//Does davener want to accept regular davening list?
	private boolean active=true;

	}
