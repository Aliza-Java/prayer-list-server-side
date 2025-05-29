package com.aliza.davening.entities;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//Davenfor = a name submitted to be davened for.
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Davenfor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull(message = "No user associated with this name. ")
	private String userEmail;

	private String category;

	@NotBlank(message = "Hebrew name is blank. ") // Regex pattern is made to
	// accept blank, so that a blank input will
	// // give only this specific message
	@NotNull(message = "Missing Hebrew name. ") 
	@Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Hebrew name must contain only Hebrew letters. ")
	private String nameHebrew;

	// Regex pattern is made to accept blank, so that a blank input will give only
	// this specific message
	@NotBlank(message = "English name is empty. ")
	@NotNull(message = "Missing English name. ") // This field is a must
	@Pattern(regexp = "^[a-zA-Z '\\-\\ ]*$", message = "English name must contain only English letters. ")
	private String nameEnglish;

	// If category is banim, need also spouse's name.

	@Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Spouse's Hebrew name must contain only Hebrew letters. ")
	private String nameHebrewSpouse;

	@Pattern(regexp = "^[a-zA-Z '\\-]*$", message = "Spouse's English name must contain only English letters. ")
	private String nameEnglishSpouse;

	// Will user himself receive this name on his list?
	private boolean submitterToReceive = true;

	private LocalDate lastConfirmedAt = LocalDate.now();

	@NotNull(message = "The name does not have an expiration date. ")
	private LocalDate expireAt;

	@NotNull(message = "The name does not have a creation date. ")
	private LocalDate createdAt = LocalDate.now();

	// When this database record was last updated (if at all)
	private LocalDate updatedAt;

	private String note;

	@Override
	public String toString() {
		return "Davenfor [" + userEmail + ", " + category + ", " + nameHebrew + ", " + nameEnglish + ", createdAt="
				+ createdAt + "]";
	}

	public boolean noSpouseInfo() {
		return (this.nameEnglishSpouse == null || this.nameEnglishSpouse.trim().length() == 0)
				&& (this.nameHebrewSpouse == null || this.nameHebrewSpouse.trim().length() == 0);
	}
}