package au.com.nab.fx.challenge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import au.com.nab.fx.challenge.entity.TradingEvent;
import au.com.nab.fx.challenge.entity.TradingIdVersion;

public interface TradingEventRepository extends JpaRepository<TradingEvent, TradingIdVersion> {

	Optional<TradingEvent> findFirstByTradeIdOrderByVersionDesc(long tradeId);

	Optional<TradingEvent> findFirstByTradeIdAndVersionLessThanOrderByVersionDesc(long tradeId, int version);

	Optional<TradingEvent> findFirstByTradeIdAndVersionGreaterThanOrderByVersionDesc(long tradeId, int version);

	List<TradingEvent> findAllByTradeIdAndVersion(long tradeId, int version);
}
