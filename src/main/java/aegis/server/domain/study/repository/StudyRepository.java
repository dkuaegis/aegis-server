package aegis.server.domain.study.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.dto.response.StudyDetailResponse;
import aegis.server.domain.study.dto.response.StudySummaryResponse;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface StudyRepository extends JpaRepository<Study, Long> {

    @Query(
            """
        SELECT new aegis.server.domain.study.dto.response.StudySummaryResponse(
            s.id, s.title, s.category, s.level,
            (SELECT COUNT(sm2) FROM StudyMember sm2 WHERE sm2.study.id = s.id AND sm2.role = :participantRole),
            s.maxParticipants, s.schedule, sm.member.name
        )
        FROM Study s
        JOIN StudyMember sm ON s.id = sm.study.id
        WHERE s.yearSemester = :yearSemester AND sm.role = :instructorRole
        """)
    List<StudySummaryResponse> findStudySummariesByYearSemester(
            YearSemester yearSemester, StudyRole instructorRole, StudyRole participantRole);

    default List<StudySummaryResponse> findStudySummariesByCurrentYearSemester() {
        return findStudySummariesByYearSemester(CURRENT_YEAR_SEMESTER, StudyRole.INSTRUCTOR, StudyRole.PARTICIPANT);
    }

    @Query(
            """
        SELECT new aegis.server.domain.study.dto.response.StudyDetailResponse(
            s.id, s.title, s.category, s.level, s.description, s.recruitmentMethod,
            (SELECT COUNT(sm2) FROM StudyMember sm2 WHERE sm2.study.id = s.id AND sm2.role = :participantRole),
            s.maxParticipants, s.schedule, s.curricula, s.qualifications, sm.member.name
        )
        FROM Study s
        JOIN StudyMember sm ON s.id = sm.study.id
        WHERE s.id = :studyId AND sm.role = :instructorRole
        """)
    Optional<StudyDetailResponse> findStudyDetailById(
            Long studyId, StudyRole instructorRole, StudyRole participantRole);

    default Optional<StudyDetailResponse> findStudyDetailById(Long studyId) {
        return findStudyDetailById(studyId, StudyRole.INSTRUCTOR, StudyRole.PARTICIPANT);
    }
}
