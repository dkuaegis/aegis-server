package aegis.server.domain.study.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import aegis.server.domain.study.domain.StudyAttendanceCode;

public interface StudyAttendanceCodeRepository extends CrudRepository<StudyAttendanceCode, String> {

    Optional<StudyAttendanceCode> findBySessionId(Long sessionId);
}
