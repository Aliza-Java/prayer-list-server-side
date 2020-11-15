package com.aliza.davening.exceptions;

@SuppressWarnings("serial")
public class ObjectNotFoundException extends Exception{

	public ObjectNotFoundException(String whatCouldNotBeFound) {
		super(whatCouldNotBeFound + " not found");
	}
	
}
