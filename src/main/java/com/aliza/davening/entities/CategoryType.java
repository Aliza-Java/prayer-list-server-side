package com.aliza.davening.entities;

import lombok.Getter;

@Getter
public enum CategoryType {
	    REFUA("רפואה"),
	    BANIM("בנים"),
	    SOLDIERS("חיילים"),
	    YESHUAH("ישועה"),
	    SHIDDUCHIM("שידוכים");
	
    private String hebName;

	CategoryType(String hebName) {
        this.hebName = hebName;
    }
}