package aegis.server.domain.study.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.study.domain.StudySession;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    @Query("SELECT ss FROM StudySession ss WHERE ss.study.id = :studyId AND ss.sessionDate = :sessionDate")
    Optional<StudySession> findByStudyIdAndSessionDate(Long studyId, LocalDate sessionDate);

    List<StudySession> findAllByStudyIdOrderBySessionDateAsc(Long studyId);
}
