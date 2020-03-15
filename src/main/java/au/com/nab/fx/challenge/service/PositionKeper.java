package au.com.nab.fx.challenge.service;

import au.com.nab.fx.challenge.model.TradeEvent;

public interface PositionKeper {

	boolean processTradeEvent(TradeEvent event);

}
