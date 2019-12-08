package exceptions;

@SuppressWarnings("serial")
public class NoRelatedEmailException extends Exception {
	
	public NoRelatedEmailException(String msg){
		super(msg);
	}

}
