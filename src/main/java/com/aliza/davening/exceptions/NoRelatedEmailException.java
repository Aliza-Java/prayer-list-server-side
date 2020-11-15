package com.aliza.davening.exceptions;

@SuppressWarnings("serial")
public class NoRelatedEmailException extends EmptyInformationException {
	
	public NoRelatedEmailException(String msg){
		super(msg);
	}

}
