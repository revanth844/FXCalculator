package au.com.nab.fx.challenge.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TradingIdVersion implements Serializable {
	private static final long serialVersionUID = 1L;

	long tradeId;
	int version;

}
