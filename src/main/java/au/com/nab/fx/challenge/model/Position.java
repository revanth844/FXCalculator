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
@Table(name = "position")
@Getter
@Setter
@NoArgsConstructor
public class Position {

	@Id
	@Column(name = "currency_pair")
	String currencyPair;

	@Column(name = "amount_base_ccy")
	BigDecimal amountBaseCcy;

	@Column(name = "amount_term_ccy")
	BigDecimal amountTermCcy;

	@Column(name = "rate")
	BigDecimal rate;

}
