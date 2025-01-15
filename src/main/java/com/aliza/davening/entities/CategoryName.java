package com.aliza.davening.entities;

import lombok.Getter;

@Getter
public enum CategoryName {
	    REFUA("רפואה"),
	    BANIM("בנים"),
	    SOLDIERS("חיילים"),
	    YESHUAH("ישועה"),
	    SHIDDUCHIM("שידוכים");
	
    private String hebName;

	CategoryName(String hebName) {
        this.hebName = hebName;
    }
	
	// Define a static method forValue
    public static CategoryName forValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return CategoryName.valueOf(value.toUpperCase()); // Convert to uppercase for case-insensitivity
    }
}