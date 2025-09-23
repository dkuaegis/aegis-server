package aegis.server.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.study.domain.StudyAttendanceReward;

public interface StudyAttendanceRewardRepository extends JpaRepository<StudyAttendanceReward, Long> {

    boolean existsByStudySessionIdAndParticipantId(Long studySessionId, Long participantId);
}
