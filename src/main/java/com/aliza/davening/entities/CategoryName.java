package com.aliza.davening.entities;

import lombok.Getter;

@Getter
public enum CategoryName {
	REFUA("רפואה", "Refua", "Refua"), SHIDDUCHIM("שידוך", "Shidduchim", "Shidduch"),
	BANIM("זרע של קיימא", "Banim", "Zera Shel Kayama"), SOLDIERS("חיילים", "Soldiers", "Soldiers"),
	YESHUA_AND_PARNASSA("ישועה ופרנסה", "Yeshua and Parnassa", "Yeshua and Parnassa");

	private String hebName;
	private String visual;
	private String listName;

	private CategoryName(String hebName, String visual, String listName) {
		this.hebName = hebName;
		this.visual = visual;
		this.listName = listName;
	}

	// Define a static method forValue
	public static CategoryName forValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		return CategoryName.valueOf(value.toUpperCase()); // Convert to uppercase for case-insensitivity
	}

	public String getVisual() {
		return visual;
	}
}