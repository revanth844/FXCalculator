package au.com.nab.fx.challenge.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trading_event")
@Getter
@Setter
@NoArgsConstructor
public class TradingEvent {

	@Id
	@Column(name = "trade_id")
	long tradeId;

	@Column(name = "version")
	int version;

	@Column(name = "event_type")
	String eventType;

	@Column(name = "ccy_pair")
	String currencyPair;

	@Column(name = "direction")
	String direction;

	@Column(name = "amount")
	BigDecimal amount;

	@Column(name = "fx_rate")
	BigDecimal fxRate;

}
