package au.com.nab.fx.challenge.repository;

import org.springframework.data.repository.CrudRepository;

import au.com.nab.fx.challenge.model.TradingEvent;

public interface TradingEventRepository extends CrudRepository<TradingEvent, Long> {

}
