package domain.gateway;

import domain.model.UHFTag;

import java.util.List;
import java.util.Optional;

public interface TagUHFRepository {
    UHFTag save(UHFTag tag);
    List<UHFTag> findAll();
    void deleteById(Long id);
    Optional<UHFTag> findById(Long id);
    UHFTag update(UHFTag tag);
    Optional<UHFTag> findByEpc(String epc);
}
