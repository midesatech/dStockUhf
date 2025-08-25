package domain.usecase;

import domain.gateway.TagUHFRepository;
import domain.model.UHFTag;

import java.util.List;
import java.util.Optional;

public class TagUHFUseCase {

    private final TagUHFRepository repo;

    public TagUHFUseCase(TagUHFRepository repo) {
        this.repo = repo;
    }

    public UHFTag save(UHFTag tag) {
        return repo.save(tag);
    }

    public List<UHFTag> findAll() {
        return repo.findAll();
    }

    public Optional<UHFTag> findById(Long id) {
        return repo.findById(id);
    }

    public UHFTag update(UHFTag tag) {
        return repo.update(tag);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Optional<UHFTag> findByEpc(String epc) {
        if (epc == null || epc.isBlank()) return Optional.empty();
        return repo.findByEpc(epc.trim());
    }
}
