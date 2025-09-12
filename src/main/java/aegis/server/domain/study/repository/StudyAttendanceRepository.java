package aegis.server.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.study.domain.StudyAttendance;

public interface StudyAttendanceRepository extends JpaRepository<StudyAttendance, Long> {

    boolean existsByStudySessionIdAndMemberId(Long studySessionId, Long memberId);
}
