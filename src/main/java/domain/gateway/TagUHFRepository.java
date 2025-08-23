package domain.gateway;

import domain.model.TagUHF;

import java.util.List;
import java.util.Optional;

public interface TagUHFRepository {
    TagUHF save(TagUHF tag);
    List<TagUHF> findAll();
    void deleteById(Long id);
    Optional<TagUHF> findById(Long id);
    TagUHF update(TagUHF tag);
    Optional<TagUHF> findByEpc(String epc);
}
