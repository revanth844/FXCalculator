package au.com.nab.fx.challenge.repository;

import org.springframework.data.repository.CrudRepository;

import au.com.nab.fx.challenge.entity.Position;

public interface PositionRepository extends CrudRepository<Position, String> {

}
