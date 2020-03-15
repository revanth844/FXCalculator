package au.com.nab.fx.challenge.repository;

import org.springframework.data.repository.CrudRepository;

import au.com.nab.fx.challenge.entity.TradingEvent;

public interface TradingEventRepository extends CrudRepository<TradingEvent, Long> {

}
