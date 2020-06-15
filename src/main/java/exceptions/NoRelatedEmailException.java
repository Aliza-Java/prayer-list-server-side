package exceptions;

@SuppressWarnings("serial")
public class NoRelatedEmailException extends EmptyInformationException {
	
	public NoRelatedEmailException(String msg){
		super(msg);
	}

}
