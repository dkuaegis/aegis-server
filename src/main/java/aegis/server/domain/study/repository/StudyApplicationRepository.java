package aegis.server.domain.study.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyApplication;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    Optional<StudyApplication> findByStudyAndMember(Study study, Member member);

    @Query("SELECT sa FROM StudyApplication sa WHERE sa.study.id = :studyId AND sa.member.id = :memberId")
    Optional<StudyApplication> findByStudyIdAndMemberId(Long studyId, Long memberId);

    @Query("SELECT sa FROM StudyApplication sa JOIN FETCH sa.member WHERE sa.study.id = :studyId")
    List<StudyApplication> findAllByStudyIdWithMember(Long studyId);

    @Query(
            "SELECT sa FROM StudyApplication sa JOIN FETCH sa.study s JOIN FETCH sa.member WHERE sa.id = :studyApplicationId")
    Optional<StudyApplication> findByIdWithStudy(Long studyApplicationId);

    @Query("SELECT COUNT(sa) > 0 FROM StudyApplication sa WHERE sa.study = :study AND sa.member = :member")
    boolean existsByStudyAndMember(Study study, Member member);
}
