package au.com.nab.fx.challenge.exception;

public class InvalidTradeEventException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidTradeEventException(String errorMessage) {
		super(errorMessage);
	}

	public InvalidTradeEventException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}
}