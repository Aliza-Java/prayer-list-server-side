package com.aliza.davening.entities;

import lombok.Getter;

@Getter
public enum CategoryName {
	    REFUA("רפואה", "Refua"),
	    SHIDDUCHIM("שידוכים", "Shidduchim"),
	    BANIM("בנים", "Banim"),
	    SOLDIERS("חיילים", "Soldiers"),
	    YESHUA_AND_PARNASSA("ישועה ופרנסה", "Yeshua and Parnassa");
	
    private String hebName;
    private String camelCase;

	CategoryName(String hebName, String camelCase) {
        this.hebName = hebName;
        this.camelCase = camelCase;
    }
	
	// Define a static method forValue
    public static CategoryName forValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return CategoryName.valueOf(value.toUpperCase()); // Convert to uppercase for case-insensitivity
    }
    
  	public String getVisual() {
  		return camelCase;    
    }
}