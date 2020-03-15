package au.com.nab.fx.challenge.model;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeEvent {
	@Positive
	long tradeId;

	@Positive
	int version;

	EventType eventType;
	TradeDirection direction;

	@NotBlank(message = "CurrencyPair cannot be blank")
	String currencyPair;

	@DecimalMin("0.0001")
	@Positive
	BigDecimal amount;

	@DecimalMin("0.0001")
	@Positive
	BigDecimal fxRate;
}
