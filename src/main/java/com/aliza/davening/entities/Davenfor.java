package com.aliza.davening.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Davenfor = a name submitted to be davened for.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Davenfor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	// TODO: fix annotations
	// @NotNull(message = "No submitter associated with this name. ")
	private String submitterEmail;

	// added jsonIgnoreProperties when fetching categoryname of Davenfor. if works
	// anyway (in SubmitterService.addDavenfor), remove this line.
	// @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	// @NotNull(message = "The name must belong to some category. ")
	@Column(name = "category")
	private String category;

	// @NotBlank(message = "Hebrew name is blank. ") // Regex pattern is made to
	// accept blank, so that a blank input will
	// // give only this specific message
	// @NotNull(message = "Missing Hebrew name. ") // This field is a must
	// Allowing only Hebrew characters, no numbers
	// @Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Hebrew name must
	// contain only Hebrew letters. ")
	private String nameHebrew;

	// Regex pattern is made to accept blank, so that a blank input will give only
	// this specific message
	// @NotBlank(message = "English name is blank. ")
	// @NotNull(message = "Missing English name. ") // This field is a must
	// Allowing only English characters, no numbers
	// @Pattern(regexp = "^[a-zA-Z '\\-\\ ]*$", message = "English name must contain
	// only English letters. ")
	private String nameEnglish;

	// If category is shidduch, need also spouse's name.

	// Allowing only Hebrew characters, no numbers
	// @Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Spouse's Hebrew
	// name must contain only Hebrew letters. ")
	private String nameHebrewSpouse;

	// If category is shidduch, need also spouse's name.
	// Allowing only English characters, no numbers
	// @Pattern(regexp = "^[a-zA-Z '\\-]*$", message = "Spouse's English name must
	// contain only English letters. ")
	private String nameEnglishSpouse;

	// Will submitter (if is a davener) receive this name on list?
	private boolean submitterToReceive = true;

	private LocalDate lastConfirmedAt = LocalDate.now();

	// @NotNull(message = "The name does not have an expiration date. ")
	private LocalDate expireAt;

	// Unix timestamp when the record was created
	// @NotNull(message = "The name does not have a creation date. ")
	private LocalDate createdAt = LocalDate.now();

	// When this database record was last updated (if at all)
	private LocalDate updatedAt;

	private String note;

	@Override
	public String toString() {
		return "Davenfor [" + submitterEmail + ", " + category + ", " + nameHebrew + ", " + nameEnglish + ", createdAt="
				+ createdAt + "]";
	}

	public boolean noSpouseInfo() {
		return this.nameEnglishSpouse == null || this.nameEnglishSpouse.trim().length() == 0
				|| this.nameHebrewSpouse == null || this.nameHebrewSpouse.trim().length() == 0;

	}

}
