package au.com.nab.fx.challenge.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import au.com.nab.fx.challenge.exception.InvalidRateEventException;
import au.com.nab.fx.challenge.service.PositionKeeper;

@RestController
@RequestMapping("print")
public class PrintController {
	@Autowired
	private PositionKeeper positionKeeper;

	@GetMapping
	public ResponseEntity<String> processRateEvent() throws InvalidRateEventException {
		String response = positionKeeper.printPositions();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}