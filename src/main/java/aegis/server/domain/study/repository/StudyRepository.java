package aegis.server.domain.study.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.GeneralStudySummary;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface StudyRepository extends JpaRepository<Study, Long> {

    @Query("SELECT s FROM Study s WHERE s.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Study> findByIdWithLock(Long id);

    @Query(
            """
        SELECT new aegis.server.domain.study.dto.response.GeneralStudySummary(
            s.id, s.title, s.category, s.level,
            s.currentParticipants,
            s.maxParticipants, s.schedule, sm.member.name
        )
        FROM Study s
        JOIN StudyMember sm ON s.id = sm.study.id
        WHERE s.yearSemester = :yearSemester AND sm.role = :instructorRole
        ORDER BY s.id DESC
        """)
    List<GeneralStudySummary> findStudySummariesByYearSemester(YearSemester yearSemester, StudyRole instructorRole);

    default List<GeneralStudySummary> findStudySummariesByCurrentYearSemester() {
        return findStudySummariesByYearSemester(CURRENT_YEAR_SEMESTER, StudyRole.INSTRUCTOR);
    }

    @Query(
            """
        SELECT new aegis.server.domain.study.dto.response.GeneralStudyDetail(
            s.id, s.title, s.category, s.level, s.description, s.recruitmentMethod,
            s.currentParticipants,
            s.maxParticipants, s.schedule, s.curricula, s.qualifications, sm.member.name
        )
        FROM Study s
        JOIN StudyMember sm ON s.id = sm.study.id
        WHERE s.id = :studyId AND sm.role = :instructorRole
        """)
    Optional<GeneralStudyDetail> findStudyDetailByIdInternal(Long studyId, StudyRole instructorRole);

    default Optional<GeneralStudyDetail> findStudyDetailById(Long studyId) {
        return findStudyDetailByIdInternal(studyId, StudyRole.INSTRUCTOR);
    }
}
