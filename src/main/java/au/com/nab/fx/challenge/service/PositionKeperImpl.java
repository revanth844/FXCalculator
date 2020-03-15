package au.com.nab.fx.challenge.service;

import static au.com.nab.fx.challenge.util.Constants.PRECISION_FOUR;

import java.math.BigDecimal;
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
import au.com.nab.fx.challenge.model.EventType;
import au.com.nab.fx.challenge.model.TradeDirection;
import au.com.nab.fx.challenge.model.TradeEvent;
import au.com.nab.fx.challenge.repository.PositionRepository;
import au.com.nab.fx.challenge.repository.TradingEventRepository;

@Service
public class PositionKeperImpl implements PositionKeper {

	Logger logger = LoggerFactory.getLogger(PositionKeperImpl.class);

	@Autowired
	PositionRepository positionRepository;
	@Autowired
	TradingEventRepository tradingEventRepository;

	@Override
	public boolean processTradeEvent(TradeEvent event) {
		TradingEvent tradingEvent = getPersistanceBean(event);
		saveTradingEvent(tradingEvent);

		Position position = getPosition(event.getCurrencyPair());
		logger.debug("position :: " + position.toString());
		if (event.getEventType().equals(EventType.NEW)) {
			createOrUpdatePosition(event, position);
		} else if (event.getEventType().equals(EventType.AMEND)) {

		}
		return true;
	}

	private Position getPosition(String ccyPair) {
		Optional<Position> currentPosition = positionRepository.findById(ccyPair);
		if (currentPosition.isPresent()) {
			return currentPosition.get();
		} else {
			return createNewPosition(ccyPair);
		}
	}

	@Transactional
	private Position createNewPosition(String ccyPair) {
		Position newPosition = new Position();
		newPosition.setCurrencyPair(ccyPair);
		newPosition.setAmountBaseCcy(new BigDecimal(0));
		newPosition.setAmountTermCcy(new BigDecimal(0));
		return newPosition;
	}

	public void createOrUpdatePosition(TradeEvent event, Position position) {
		position.setRate(event.getFxRate());
		if (event.getDirection().equals(TradeDirection.BUY)) {
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

	private void savePosition(Position position) {
		try {
			positionRepository.save(position);
		} catch (ConstraintViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage() + ". " + ex.getCause(), ex);
		}
	}

	private void saveTradingEvent(TradingEvent tradingEvent) {
		try {
			tradingEventRepository.save(tradingEvent);
		} catch (ConstraintViolationException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage() + ". " + ex.getCause(), ex);
		}
	}

	private TradingEvent getPersistanceBean(TradeEvent event) {
		ModelMapper modelMapper = new ModelMapper();
		return modelMapper.map(event, TradingEvent.class);
	}

	private BigDecimal getConvertedAmount(BigDecimal amount, BigDecimal rate) {
		return amount.multiply(rate, PRECISION_FOUR);
	}
}
