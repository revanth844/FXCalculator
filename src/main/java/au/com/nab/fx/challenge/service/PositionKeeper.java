package au.com.nab.fx.challenge.service;

import au.com.nab.fx.challenge.exception.InvalidTradeEventException;
import au.com.nab.fx.challenge.model.TradeEvent;

public interface PositionKeeper {

	/**
	 * Wrapper method to be consumed from controller
	 * 
	 * @param event
	 * @return
	 * @throws InvalidTradeEventException
	 */
	boolean processTradeEvent(TradeEvent event) throws InvalidTradeEventException;

}
