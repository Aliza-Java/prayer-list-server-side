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
@AllArgsConstructor
@NoArgsConstructor
@ToString

@Entity
public class Parasha {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;

	String englishName;

	String hebrewName;

	String fileName;

}
