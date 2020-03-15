package au.com.nab.fx.challenge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import au.com.nab.fx.challenge.model.TradeEvent;
import au.com.nab.fx.challenge.service.PositionKeper;

@RestController
@RequestMapping("trade")
public class TradeController {
	@Autowired
	private PositionKeper positionKeper;

	@PostMapping(value = "event", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> processTradeEvent(@RequestBody TradeEvent event) {
		return (positionKeper.processTradeEvent(event) ? new ResponseEntity<Void>(HttpStatus.CREATED)
				: new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

}