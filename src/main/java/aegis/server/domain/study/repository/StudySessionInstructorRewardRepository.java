package aegis.server.domain.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.study.domain.StudySessionInstructorReward;

public interface StudySessionInstructorRewardRepository extends JpaRepository<StudySessionInstructorReward, Long> {

    boolean existsByStudySessionIdAndInstructorId(Long studySessionId, Long instructorId);
}
