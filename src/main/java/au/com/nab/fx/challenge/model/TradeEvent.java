package au.com.nab.fx.challenge.model;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;

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
	long tradeId;
	int version;
	EventType eventType;
	TradeDirection direction;

	@NotBlank(message = "CurrencyPair cannot be blank")
	String currencyPair;

	@DecimalMin("0.0001")
	BigDecimal amount;

	@DecimalMin("0.0001")
	BigDecimal fxRate;
}
