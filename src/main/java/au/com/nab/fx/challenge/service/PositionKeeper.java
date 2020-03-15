package au.com.nab.fx.challenge.service;

import au.com.nab.fx.challenge.exception.InvalidRateEventException;
import au.com.nab.fx.challenge.exception.InvalidTradeEventException;
import au.com.nab.fx.challenge.model.RateEvent;
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

	/**
	 * Method to be consumed from controller to cancel trade
	 * 
	 * @param tradeId
	 * @return
	 * @throws InvalidTradeEventException
	 */
	boolean cancelTradeEvent(long tradeId) throws InvalidTradeEventException;

	/**
	 * Method to process rate change event
	 * 
	 * @param event
	 * @return
	 * @throws InvalidRateEventException
	 */
	boolean processRateEvent(RateEvent event) throws InvalidRateEventException;

	/**
	 * Print positions current positions in CSV format
	 * 
	 * @return
	 */
	String printPositions();
}
