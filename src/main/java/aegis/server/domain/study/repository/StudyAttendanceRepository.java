package aegis.server.domain.study.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.study.domain.StudyAttendance;

public interface StudyAttendanceRepository extends JpaRepository<StudyAttendance, Long> {

    boolean existsByStudySessionIdAndMemberId(Long studySessionId, Long memberId);

    @Query(
            """
        SELECT sa.member.id AS memberId, sa.studySession.id AS sessionId
        FROM StudyAttendance sa
        JOIN sa.studySession ss
        WHERE ss.study.id = :studyId
        """)
    List<AttendanceMemberSessionPair> findMemberSessionPairsByStudyId(Long studyId);
}
