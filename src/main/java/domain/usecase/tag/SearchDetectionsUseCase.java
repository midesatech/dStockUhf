package domain.usecase.tag;

import domain.gateway.SearchRepository;
import domain.model.DetectionRecord;
import domain.model.Occupant;
import domain.model.PathHop;

import java.time.LocalDateTime;
import java.util.List;

public class SearchDetectionsUseCase {

    private final SearchRepository repo;

    public SearchDetectionsUseCase(SearchRepository repo) { this.repo = repo; }

    private static String norm(String subject) {
        if (subject == null) subject = "EMPLOYEE";
        subject = subject.toUpperCase();
        if (!subject.equals("EMPLOYEE") && !subject.equals("EQUIPMENT"))
            throw new IllegalArgumentException("subject must be EMPLOYEE or EQUIPMENT");
        return subject;
    }
    private static void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new IllegalArgumentException("start/end required");
        if (end.isBefore(start)) throw new IllegalArgumentException("end before start");
    }

    public List<Occupant> searchBy(String subject, LocalDateTime start, LocalDateTime end) {
        subject = norm(subject); validateRange(start, end);
        return repo.searchBySubjectAndTime(subject, start, end);
    }

    public List<DetectionRecord> searchRaw(String subject, LocalDateTime start, LocalDateTime end) {
        subject = norm(subject); validateRange(start, end);
        return repo.searchRawBySubjectAndTime(subject, start, end);
    }

    public List<PathHop> routeForEpc(String epc, LocalDateTime start, LocalDateTime end) {
        validateRange(start, end);
        if (epc == null || epc.isBlank()) throw new IllegalArgumentException("epc required");
        return repo.pathForEpc(epc, start, end);
    }
}