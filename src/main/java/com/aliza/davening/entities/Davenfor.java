package com.aliza.davening.entities;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//Davenfor = a name submitted to be davened for.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
public class Davenfor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NotNull(message = "No submitter associated with this name. ")
	@ManyToOne(fetch = FetchType.EAGER)
	private Submitter submitter;

	// Which type of cause does this person need to be davened for.
	@ManyToOne(fetch = FetchType.EAGER)
	@NotNull(message = "The name must belong to some category. ")
	private Category category;

	@NotBlank(message = "Hebrew name is blank. ") // Regex pattern is made to accept blank, so that a blank input will											// give only this specific message
	@NotNull(message = "Missing Hebrew name. ") // This field is a must
	// Allowing only Hebrew characters, no numbers
	@Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Hebrew name must contain only Hebrew letters. ") 
	private String nameHebrew;
	
	// Regex pattern is made to accept blank, so that a blank input will give only
	// this specific message
	@NotBlank(message = "English name is blank. ")
	@NotNull(message = "Missing English name. ") // This field is a must
	// Allowing only English characters, no numbers
	@Pattern(regexp = "^[a-zA-Z '\\-\\ ]*$", message = "English name must contain only English letters. ")
	private String nameEnglish;

	// If category is shidduch, need also spouse's name.
	// Regex pattern is made to accept blank, so that a blank input will give only
	// this specific message
	@NotBlank(message = "Spouse's Hebrew name is blank. ")
	@Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Spouse's Hebrew name must contain only Hebrew letters. ") // Allowing
																														// only
																														// Hebrew
																														// characters,
																														// no
																														// numbers
	private String nameHebrewSpouse;

	// If category is shidduch, need also spouse's name.
	@NotBlank(message = "Spouse's English name is blank. ") // Regex pattern is made to accept blank, so that a blank
															// input will give only this specific message
	@Pattern(regexp = "^[a-zA-Z '\\-]*$", message = "Spouse's English name must contain only English letters. ") // Allowing
																													// only
																													// English
																													// characters,
																													// no
																													// numbers
	private String nameEnglishSpouse;

	// Will submitter (if is a davener) receive this name on list?
	private boolean submitterToReceive = true;

	private String lastConfirmedAt = LocalDate.now().toString();

	@NotBlank(message = "The name does not have an expiration date. ") // This will show up in null case as well.
	private String expireAt;

	// Unix timestamp when the record was created
	@NotBlank(message = "The name does not have a creation date. ") // This will show up in null case as well.
	private String createdAt = LocalDate.now().toString();

	// When this database record was last updated (if at all)
	private String updatedAt;
}
