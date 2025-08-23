package domain.usecase;

import domain.gateway.TagUHFRepository;
import domain.model.TagUHF;

import java.util.List;
import java.util.Optional;

public class TagUHFUseCase {

    private final TagUHFRepository repo;

    public TagUHFUseCase(TagUHFRepository repo) {
        this.repo = repo;
    }

    public TagUHF save(TagUHF tag) {
        return repo.save(tag);
    }

    public List<TagUHF> findAll() {
        return repo.findAll();
    }

    public Optional<TagUHF> findById(Long id) {
        return repo.findById(id);
    }

    public TagUHF update(TagUHF tag) {
        return repo.update(tag);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Optional<TagUHF> findByEpc(String epc) {
        if (epc == null || epc.isBlank()) return Optional.empty();
        return repo.findByEpc(epc.trim());
    }
}
