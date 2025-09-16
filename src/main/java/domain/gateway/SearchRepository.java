package domain.gateway;

import domain.model.DetectionRecord;
import domain.model.Occupant;
import domain.model.PathHop;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchRepository {
    List<Occupant> searchBySubjectAndTime(String subject, LocalDateTime start, LocalDateTime end);
    List<DetectionRecord> searchRawBySubjectAndTime(String subject, LocalDateTime start, LocalDateTime end);
    List<PathHop> pathForEpc(String epc, LocalDateTime start, LocalDateTime end);
}
