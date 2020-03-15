package au.com.nab.fx.challenge.exception;

public class InvalidRateEventException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidRateEventException(String errorMessage) {
		super(errorMessage);
	}

	public InvalidRateEventException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}
}