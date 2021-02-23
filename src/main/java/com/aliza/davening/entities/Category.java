package com.aliza.davening.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//No ToString in order to override and write one without Davenfors (causes recursive Json)

//Different types of davening lists e.g. shidduchim, banim etc.
@Entity
public class Category {

	// A constructor for initializing the database.
	public Category(long id, String english, String hebrew, boolean current, int updateRate, int catOrder) {
		this.id = id;
		this.english = english;
		this.hebrew = hebrew;
		this.isCurrent = current;
		this.updateRate = updateRate;
		this.catOrder = catOrder;
	}

	public Category(String english, String hebrew, boolean current, int updateRate, int catOrder) {
		this.english = english;
		this.hebrew = hebrew;
		this.isCurrent = current;
		this.updateRate = updateRate;
		this.catOrder = catOrder;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(unique = true)
	@NotNull(message = "Category's English name cannot be null. ")
	@NotBlank(message = "Category's English name cannot be blank. ")
	@Pattern(regexp = "^[a-zA-Z '\\-\\ ]*$", message = "Category's English name must contain only English letters and spaces. ")
	private String english;

	@NotNull(message = "Category's Hebrew name cannot be null. ")
	@NotBlank(message = "Category's Hebrew name cannot be blank. ")
	@Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Category's Hebrew name must contain only Hebrew letters and spaces. ")
	private String hebrew;

	// Is this the current category being sent out?
	private boolean isCurrent;

	// How many days a name of this category adds on when extending expiration date
	@Positive(message = "The update rate of a category must be a positive number. ")
	private int updateRate;

	// The order in which the categories get sent out.
	@Positive(message = "The category order must be represented by a positive number. ")
	private int catOrder;

	@OneToMany(cascade = { CascadeType.REMOVE }, mappedBy = "category")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	@JsonIgnore
	private List<Davenfor> davenfors;

	// davenfors omitted as it causes recursive output.
	@Override
	public String toString() {
		return "Category [id=" + id + ", english=" + english + ", hebrew=" + hebrew + ", isCurrent=" + isCurrent
				+ ", updateRate=" + updateRate + ", catOrder=" + catOrder + "]";
	}

}
