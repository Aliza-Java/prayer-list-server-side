package com.aliza.davening.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

//Different types of davening lists e.g. shidduchim, banim etc.
@Entity
public class Category {
	// TODO*: in future, make repository tests for all validations

	public final static String BANIM = "BANIM";

	public Category(CategoryName cname, boolean current, int updateRate, int catOrder) {
		this.cname = cname;
		this.isCurrent = current;
		this.updateRate = updateRate;
		this.catOrder = catOrder;
	}

	// populated upon start of program, from DB.
	// When allow for adding categories - need to change them in this list too (or
	// fetch all again);
	public static List<Category> categories;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	// TODO*: In future, when allow admin to add more categories, enable validations
	// somehow in cname fields
//	@Column(unique = true)
//	@NotNull(message = "Category's English name cannot be null. ")
//	@NotBlank(message = "Category's English name cannot be blank. ")
//	@Pattern(regexp = "^[a-zA-Z '\\-\\ ]*$", message = "Category's English name must contain only English letters and spaces. ")
//	private String english;
//
//	@NotNull(message = "Category's Hebrew name cannot be null. ")
//	@NotBlank(message = "Category's Hebrew name cannot be blank. ")
//	@Pattern(regexp = "^[\\u0590-\\u05fe '\\-]*$", message = "Category's Hebrew name must contain only Hebrew letters and spaces. ")
//	private String hebrew;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true)
	private CategoryName cname;

	// Is this the current category being sent out?
	private boolean isCurrent;

	// How many days a name of this category adds on when extending expiration date
	@Positive(message = "The update rate of a category must be a positive number")
	private int updateRate;

	// The order in which the categories get sent out.
	@Positive(message = "The category order must be represented by a positive number. ")
	private int catOrder;

	@Override
	public String toString() {
		return "Category[ " + id + " " + cname + " isCurrent: " + isCurrent + "]";
	}

	public static Category getCategory(String name) {
		return categories.stream().filter(c -> name.equalsIgnoreCase(c.getCname().name())).findFirst().orElse(null);
	}

	@JsonValue
	public String toJson() {
		return cname.toString().toLowerCase();
	}

	public static boolean isBanim(String catString) {
		return BANIM.equalsIgnoreCase(catString);
	}
}
