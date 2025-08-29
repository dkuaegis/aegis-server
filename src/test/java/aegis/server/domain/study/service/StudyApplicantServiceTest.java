package aegis.server.domain.study.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationDetail;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationStatus;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class StudyApplicantServiceTest extends IntegrationTest {

    @Autowired
    StudyApplicantService studyApplicantService;

    @Autowired
    StudyApplicationRepository studyApplicationRepository;

    @Autowired
    StudyRepository studyRepository;

    @Nested
    class 스터디_신청_상태_조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();
            StudyApplication application = createStudyApplication(study, member, "신청 사유");

            // when
            ApplicantStudyApplicationStatus response =
                    studyApplicantService.getStudyApplicationStatus(study.getId(), userDetails);

            // then
            assertEquals(application.getId(), response.studyApplicationId());
            assertEquals(StudyApplicationStatus.PENDING, response.status());
        }

        @Test
        void 신청하지_않은_스터디를_조회하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyApplicantService.getStudyApplicationStatus(study.getId(), userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_신청_상세_조회 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();
            StudyApplication application = createStudyApplication(study, member, "신청 사유입니다");

            // when
            ApplicantStudyApplicationDetail response =
                    studyApplicantService.getStudyApplicationDetail(study.getId(), userDetails);

            // then
            assertEquals(application.getId(), response.studyApplicationId());
            assertEquals(StudyApplicationStatus.PENDING, response.status());
            assertEquals("신청 사유입니다", response.applicationReason());
            assertEquals(study.getTitle(), response.studyTitle());
            assertEquals(study.getDescription(), response.studyDescription());
        }

        @Test
        void 신청하지_않은_스터디를_조회하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyApplicantService.getStudyApplicationDetail(study.getId(), userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_신청_사유_수정 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();
            StudyApplication application = createStudyApplication(study, member, "기존 신청 사유");
            StudyEnrollRequest request = new StudyEnrollRequest("수정된 신청 사유");

            // when
            ApplicantStudyApplicationDetail response =
                    studyApplicantService.updateStudyApplicationReason(study.getId(), request, userDetails);

            // then
            // 반환값 검증
            assertEquals(application.getId(), response.studyApplicationId());
            assertEquals(StudyApplicationStatus.PENDING, response.status());
            assertEquals("수정된 신청 사유", response.applicationReason());
            assertEquals(study.getTitle(), response.studyTitle());
            assertEquals(study.getDescription(), response.studyDescription());

            // DB 상태 검증
            StudyApplication updatedApplication =
                    studyApplicationRepository.findById(application.getId()).get();
            assertEquals("수정된 신청 사유", updatedApplication.getApplicationReason());
        }

        @Test
        void 신청하지_않은_스터디를_수정하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();
            StudyEnrollRequest request = new StudyEnrollRequest("신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyApplicantService.updateStudyApplicationReason(study.getId(), request, userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 승인된_신청을_수정하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy();
            StudyApplication application = createStudyApplication(study, member, "기존 신청 사유");
            application.approve(); // 승인 상태로 변경
            StudyEnrollRequest request = new StudyEnrollRequest("수정된 신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyApplicantService.updateStudyApplicationReason(study.getId(), request, userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_ALREADY_APPROVED, exception.getErrorCode());
        }
    }

    private Study createStudy() {
        Study study = Study.create(
                "테스트 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "테스트용 스터디 설명",
                StudyRecruitmentMethod.APPLICATION,
                10,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        return studyRepository.save(study);
    }

    private StudyApplication createStudyApplication(Study study, Member member, String applicationReason) {
        StudyApplication studyApplication = StudyApplication.create(study, member, applicationReason);
        return studyApplicationRepository.save(studyApplication);
    }
}
