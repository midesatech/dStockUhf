package domain.gateway;

import domain.model.PeopleType;

import java.util.List;
import java.util.Optional;

public interface PeopleTypeRepository {
    PeopleType save(PeopleType t);
    List<PeopleType> findAll();
    void deleteById(Long id);
    Optional<PeopleType> findById(Long id);
    PeopleType update(PeopleType t);
}
