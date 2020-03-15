package au.com.nab.fx.challenge.service;

import static au.com.nab.fx.challenge.util.Constants.PRECISION_FOUR_DECIMALS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import au.com.nab.fx.challenge.entity.Position;
import au.com.nab.fx.challenge.entity.TradingEvent;
import au.com.nab.fx.challenge.entity.TradingIdVersion;
import au.com.nab.fx.challenge.exception.InvalidTradeEventException;
import au.com.nab.fx.challenge.model.EventType;
import au.com.nab.fx.challenge.model.TradeDirection;
import au.com.nab.fx.challenge.model.TradeEvent;
import au.com.nab.fx.challenge.repository.PositionRepository;
import au.com.nab.fx.challenge.repository.TradingEventRepository;

@Service
public class PositionKeeperImpl implements PositionKeeper {

	Logger logger = LoggerFactory.getLogger(PositionKeeperImpl.class);

	@Autowired
	PositionRepository positionRepository;
	@Autowired
	TradingEventRepository tradingEventRepository;

	@Override
	@Transactional
	public boolean processTradeEvent(TradeEvent event) throws InvalidTradeEventException {

		if (event.getEventType().equals(EventType.NEW)) {
			processNewEvent(event);
		} else if (event.getEventType().equals(EventType.AMEND)) {
			processAmendEvent(event);
		}
		return true;
	}

	/**
	 * Process new event
	 * 
	 * @param event
	 * @throws InvalidTradeEventException
	 */
	private void processNewEvent(TradeEvent event) throws InvalidTradeEventException {
		if (event.getVersion() != 0) {
			throw new InvalidTradeEventException("NEW trade should have version=0");
		}
		Position position = getPosition(event.getCurrencyPair());
		logger.debug("position :: " + position.toString());

		TradingEvent tradingEvent = getPersistenceBean(event);
		if (!anyForwardTradeProcessed(event, tradingEvent)) {
			saveTradingEvent(tradingEvent);
			createOrUpdatePosition(tradingEvent, position);
		}
	}

	/**
	 * Process Amend event
	 * 
	 * @param event
	 * @throws InvalidTradeEventException
	 */
	private void processAmendEvent(TradeEvent event) throws InvalidTradeEventException {
		// Error when 'Amend' on version=0
		if (event.getVersion() == 0) {
			throw new InvalidTradeEventException("Cannot AMEND trade for version=0");
		}

		TradingEvent tradingEvent = getPersistenceBean(event);
		if (!anyForwardTradeProcessed(event, tradingEvent)) {
			// If event already processed, error will be returned
			saveTradingEvent(tradingEvent);

			Position position = getPosition(event.getCurrencyPair());
			logger.debug("position :: " + position.toString());

			// Reverse the position updates associated with this trade, using the previous
			// version’s rate and base currency.
			Optional<TradingEvent> lastVersionTradingEvent = getLastTradingEvent(event.getTradeId(),
					event.getVersion());
			if (lastVersionTradingEvent.isPresent()) {
				if (lastVersionTradingEvent.get().getDirection().equals(tradingEvent.getDirection())) {
					reverseEarlierPosition(lastVersionTradingEvent.get(), position);
				} else {
					tradingEvent.setIgnored(true);
					updateTradingEvent(tradingEvent);
					throw new InvalidTradeEventException("Cannot change direction of trade");
				}
			}

			// Update the associated position based on the amended trade’s Direction
			createOrUpdatePosition(tradingEvent, position);
		}
	}

	/**
	 * Ignore current version if any Forward versions are already processed.
	 * 
	 * @param event
	 * @param tradingEvent
	 * @return
	 */
	private boolean anyForwardTradeProcessed(TradeEvent event, TradingEvent tradingEvent) {
		// If an amend is received before the initial new trade, apply the amended
		// trade to the position, when the original new trade arrives it is ignored
		Optional<TradingEvent> forwardTradingEvent = getForwardTradingEvent(event.getTradeId(), event.getVersion());
		if (forwardTradingEvent.isPresent()) {
			// Any incoming amends with a lower version number are to be ignored
			tradingEvent.setIgnored(true);
			saveTradingEvent(tradingEvent);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get Position entity for persistence. If present, get current position details
	 * of given currencyPair. Else, create a new entity object to persist.
	 * 
	 * @param ccyPair
	 * @return
	 */
	private Position getPosition(String ccyPair) {
		Optional<Position> currentPosition = positionRepository.findById(ccyPair);
		if (currentPosition.isPresent()) {
			return currentPosition.get();
		} else {
			return createNewPosition(ccyPair);
		}
	}

	/**
	 * Create new Position entity object to persist.
	 * 
	 * @param ccyPair
	 * @return
	 */
	private Position createNewPosition(String ccyPair) {
		Position newPosition = new Position();
		newPosition.setCurrencyPair(ccyPair);
		newPosition.setAmountBaseCcy(new BigDecimal(0));
		newPosition.setAmountTermCcy(new BigDecimal(0));
		return newPosition;
	}

	/**
	 * Reverse position metrics from the last event retrieved. If no earlier
	 * versions are present, this method will not be invoked
	 * 
	 * @param event
	 * @param position
	 */
	public void reverseEarlierPosition(TradingEvent event, Position position) {
		logger.debug("ReverseEarlierPosition for " + event);
		event.setDirection(event.getDirection().equals(TradeDirection.BUY.toString()) ? TradeDirection.SELL.toString()
				: TradeDirection.BUY.toString());
		createOrUpdatePosition(event, position);
	}

	/**
	 * Create/Update the position for given currency pair
	 * 
	 * @param event
	 * @param position
	 */
	private void createOrUpdatePosition(TradingEvent event, Position position) {
		logger.debug("createOrUpdatePosition for " + event);

		position.setRate(event.getFxRate());
		if (event.getDirection().equals(TradeDirection.BUY.toString())) {
			position.setAmountBaseCcy(position.getAmountBaseCcy().add(event.getAmount()));
			position.setAmountTermCcy(
					position.getAmountTermCcy().subtract(getConvertedAmount(event.getAmount(), event.getFxRate())));
		} else { // TradeDirection.SELL
			position.setAmountBaseCcy(
					position.getAmountBaseCcy().subtract(getConvertedAmount(event.getAmount(), event.getFxRate())));
			position.setAmountTermCcy(position.getAmountTermCcy().add(event.getAmount()));
		}
		logger.debug("createOrUpdatePosition() " + position);
		savePosition(position);
	}

	/**
	 * Save poistion details
	 * 
	 * @param position
	 */
	private void savePosition(Position position) {
		try {
			positionRepository.save(position);
		} catch (ConstraintViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage() + ". " + ex.getCause(), ex);
		}
	}

	/**
	 * Update TradingEvent in ignored scenario
	 * 
	 * @param tradingEvent
	 */
	private void updateTradingEvent(TradingEvent tradingEvent) {
		tradingEventRepository.save(tradingEvent);
	}

	/**
	 * Save TradingEvent if not already processed
	 * 
	 * @param tradingEvent
	 */
	private void saveTradingEvent(TradingEvent tradingEvent) {
		isTradingEventProcessed(tradingEvent);
		logger.debug("saveTradingEvent() for " + tradingEvent);
		tradingEventRepository.save(tradingEvent);
	}

	/**
	 * Throw an error if TradingEvent is already processed
	 * 
	 * @param tradingEvent
	 */
	private void isTradingEventProcessed(TradingEvent tradingEvent) {
		Optional<TradingEvent> existingEvent = tradingEventRepository
				.findById(new TradingIdVersion(tradingEvent.getTradeId(), tradingEvent.getVersion()));
		if (existingEvent.isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "TradeEvent already processed",
					new Exception("TradeEvent already processed"));
		}
	}

	/**
	 * This method assumes that versions will be received in the same sequence
	 * 
	 * @param tradeId
	 * @return
	 */
	private Optional<TradingEvent> getLastTradingEvent(long tradeId) {
		return tradingEventRepository.findFirstByTradeIdOrderByVersionDesc(tradeId);
	}

	/**
	 * This method relies of DB-engine to get the last tradingEvent, if any
	 * 
	 * @param tradeId
	 * @param version
	 * @return
	 */
	private Optional<TradingEvent> getLastTradingEvent(long tradeId, int version) {
		return tradingEventRepository.findFirstByTradeIdAndVersionLessThanOrderByVersionDesc(tradeId, version);
	}

	/**
	 * If DB data is huge and growing fast, better to use PK-index to fetch data and
	 * process in Java instead
	 * 
	 * @param tradeId
	 * @param version
	 * @return
	 */
	private Optional<TradingEvent> getLastTradingEventUsingDBIndexAndJava8(long tradeId, int version) {
		Comparator<TradingEvent> comparator = Comparator.comparingInt(TradingEvent::getVersion);
		return tradingEventRepository.findAllByTradeIdAndVersion(tradeId, version).stream()
				.sorted(comparator.reversed()).filter(trade -> (trade.getVersion() < version)).findFirst();
	}

	/**
	 * This method retrieves any future AMEND records for this tradeId, if any
	 * 
	 * @param tradeId
	 * @param version
	 * @return
	 */
	private Optional<TradingEvent> getForwardTradingEvent(long tradeId, int version) {
		return tradingEventRepository.findFirstByTradeIdAndVersionGreaterThanOrderByVersionDesc(tradeId, version);
	}

	/**
	 * This method retrieves any future AMEND records for this tradeId, if any If DB
	 * data is huge and growing fast, better to use PK-index to fetch data and
	 * process in Java instead
	 * 
	 * @param tradeId
	 * @param version
	 * @return
	 */
	private Optional<TradingEvent> getForwardTradingEventUsingDBIndexAndJava8(long tradeId, int version) {
		Comparator<TradingEvent> comparator = Comparator.comparingInt(TradingEvent::getVersion);
		return tradingEventRepository.findAllByTradeIdAndVersion(tradeId, version).stream()
				.sorted(comparator.reversed()).filter(trade -> (trade.getVersion() < version)).findFirst();
	}

	/**
	 * Get Persistence bean from request object
	 * 
	 * @param event
	 * @return
	 */
	private TradingEvent getPersistenceBean(TradeEvent event) {
		ModelMapper modelMapper = new ModelMapper();
		return modelMapper.map(event, TradingEvent.class);
	}

	/**
	 * Use half-up rounding mode and up to four decimal points precision
	 * 
	 * @param amount
	 * @param rate
	 * @return
	 */
	private BigDecimal getConvertedAmount(BigDecimal amount, BigDecimal rate) {
		return amount.multiply(rate).setScale(PRECISION_FOUR_DECIMALS, RoundingMode.HALF_UP);

	}
}
