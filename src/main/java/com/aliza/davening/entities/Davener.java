package com.aliza.davening.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
//TODO: consider getting rid of davener, leaving submitter.  can add a submitter without him giving names. really same thing.  or other way - davener can submit and opt not to get list. 
@Entity
public class Davener {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	//TODO: fix annotations
	//@NotBlank(message = "Davener must have a country. ")
	private String country = "Israel";

	//@NotBlank(message = "Davener must have an associated email. ")
	//@Pattern(regexp = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,5}))?$", message = "Davener email seems to be invalid.")
	private String email;

	//@Pattern(regexp = "^[0-9]+$", message = "Whatsapp number can contain only numeric digits.")
	private String whatsapp;

	// Does davener want to accept weekly davening list and alerts?
	private boolean active = true;
	
	public Davener(Davener current){
		this.country = current.country;
		this.email = current.email;
		this.whatsapp = current.whatsapp;
		this.active = current.active;
	}

}
