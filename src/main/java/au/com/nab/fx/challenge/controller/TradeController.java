package au.com.nab.fx.challenge.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.com.nab.fx.challenge.exception.InvalidTradeEventException;
import au.com.nab.fx.challenge.model.TradeEvent;
import au.com.nab.fx.challenge.service.PositionKeeper;

@RestController
@RequestMapping("trade")
public class TradeController {
	@Autowired
	private PositionKeeper positionKeeper;

	@PostMapping(value = "event", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> processTradeEvent(@Valid @RequestBody TradeEvent event)
			throws InvalidTradeEventException {
		boolean response = positionKeeper.processTradeEvent(event);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(value = "cancel", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> cancelTradeEvent(@Valid @RequestParam long tradeId)
			throws InvalidTradeEventException {
		boolean response = positionKeeper.cancelTradeEvent(tradeId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}