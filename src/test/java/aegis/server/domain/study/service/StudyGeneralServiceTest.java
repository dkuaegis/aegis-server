package aegis.server.domain.study.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.GeneralStudySummary;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class StudyGeneralServiceTest extends IntegrationTest {

    @Autowired
    StudyGeneralService studyGeneralService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    StudyApplicationRepository studyApplicationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Nested
    class 모든_스터디_조회 {

        @Test
        void 성공한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Study study1 = createStudy("스터디1", StudyRecruitmentMethod.APPLICATION);
            Study study2 = createStudy("스터디2", StudyRecruitmentMethod.FCFS);
            createStudyMember(study1, instructor1, StudyRole.INSTRUCTOR);
            createStudyMember(study2, instructor2, StudyRole.INSTRUCTOR);

            // when
            List<GeneralStudySummary> response = studyGeneralService.findAllStudies();

            // then
            assertEquals(2, response.size());
            assertTrue(response.stream().anyMatch(summary -> summary.id().equals(study1.getId())));
            assertTrue(response.stream().anyMatch(summary -> summary.id().equals(study2.getId())));
        }

        @Test
        void 스터디가_없는_경우_빈_리스트를_반환한다() {
            // when
            List<GeneralStudySummary> response = studyGeneralService.findAllStudies();

            // then
            assertEquals(0, response.size());
        }
    }

    @Nested
    class 스터디_상세_조회 {

        @Test
        void 성공한다() {
            // given
            Member instructor = createMember();
            Study study = createStudy("테스트 스터디", StudyRecruitmentMethod.APPLICATION);
            createStudyMember(study, instructor, StudyRole.INSTRUCTOR);

            // when
            GeneralStudyDetail response = studyGeneralService.getStudyDetail(study.getId());

            // then
            assertEquals(study.getId(), response.id());
            assertEquals(study.getTitle(), response.title());
            assertEquals(study.getCategory(), response.category());
            assertEquals(study.getLevel(), response.level());
            assertEquals(study.getDescription(), response.description());
        }

        @Test
        void 존재하지_않는_스터디를_조회하면_예외가_발생한다() {
            // given
            Long nonExistentStudyId = 999L;

            // when & then
            CustomException exception =
                    assertThrows(CustomException.class, () -> studyGeneralService.getStudyDetail(nonExistentStudyId));
            assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_생성 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when
            GeneralStudyDetail response = studyGeneralService.createStudy(request, userDetails);

            // then
            // 반환값 검증
            assertEquals(request.title(), response.title());
            assertEquals(request.category(), response.category());
            assertEquals(request.level(), response.level());
            assertEquals(request.description(), response.description());
            assertEquals(request.recruitmentMethod(), response.recruitmentMethod());

            // DB 상태 검증
            Study createdStudy = studyRepository.findById(response.id()).get();
            assertEquals(request.title(), createdStudy.getTitle());
            assertEquals(request.category(), createdStudy.getCategory());
            assertEquals(request.level(), createdStudy.getLevel());

            // StudyMember 생성 검증
            StudyMember studyMember = studyMemberRepository
                    .findByStudyAndMember(createdStudy, member)
                    .get();
            assertEquals(StudyRole.INSTRUCTOR, studyMember.getRole());
        }
    }

    @Nested
    class 스터디_참가_신청_FCFS방식 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy("FCFS 스터디", StudyRecruitmentMethod.FCFS);
            StudyEnrollRequest request = new StudyEnrollRequest("참가 사유");

            // when
            studyGeneralService.enrollInStudy(study.getId(), request, userDetails);

            // then
            // StudyMember 생성 검증
            StudyMember studyMember =
                    studyMemberRepository.findByStudyAndMember(study, member).get();
            assertEquals(StudyRole.PARTICIPANT, studyMember.getRole());

            // currentParticipants 증가 검증
            Study updatedStudy = studyRepository.findById(study.getId()).get();
            assertEquals(1, updatedStudy.getCurrentParticipants());
        }

        @Test
        void 이미_등록된_사용자가_중복_신청하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy("FCFS 스터디", StudyRecruitmentMethod.FCFS);
            createStudyMember(study, member, StudyRole.PARTICIPANT);
            StudyEnrollRequest request = new StudyEnrollRequest("참가 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyGeneralService.enrollInStudy(study.getId(), request, userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 정원이_가득_찬_스터디에_신청하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudyWithMaxParticipants(1, StudyRecruitmentMethod.FCFS);
            study.increaseCurrentParticipant(); // 정원 가득 채움
            StudyEnrollRequest request = new StudyEnrollRequest("참가 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyGeneralService.enrollInStudy(study.getId(), request, userDetails));
            assertEquals(ErrorCode.STUDY_FULL, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_스터디에_신청하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Long nonExistentStudyId = 999L;
            StudyEnrollRequest request = new StudyEnrollRequest("참가 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyGeneralService.enrollInStudy(nonExistentStudyId, request, userDetails));
            assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_참가_신청_APPLICATION방식 {

        @Test
        void 성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy("신청서 스터디", StudyRecruitmentMethod.APPLICATION);
            StudyEnrollRequest request = new StudyEnrollRequest("신청 사유입니다");

            // when
            studyGeneralService.enrollInStudy(study.getId(), request, userDetails);

            // then
            // StudyApplication 생성 검증
            StudyApplication studyApplication = studyApplicationRepository
                    .findByStudyAndMember(study, member)
                    .get();
            assertEquals("신청 사유입니다", studyApplication.getApplicationReason());
            assertEquals(StudyApplicationStatus.PENDING, studyApplication.getStatus());
        }

        @Test
        void 이미_등록된_사용자가_중복_신청하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy("신청서 스터디", StudyRecruitmentMethod.APPLICATION);
            createStudyMember(study, member, StudyRole.PARTICIPANT);
            StudyEnrollRequest request = new StudyEnrollRequest("신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyGeneralService.enrollInStudy(study.getId(), request, userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 이미_신청한_사용자가_중복_신청하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Study study = createStudy("신청서 스터디", StudyRecruitmentMethod.APPLICATION);
            createStudyApplication(study, member, "기존 신청");
            StudyEnrollRequest request = new StudyEnrollRequest("새로운 신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyGeneralService.enrollInStudy(study.getId(), request, userDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_ALREADY_EXISTS, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_스터디에_신청하면_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            Long nonExistentStudyId = 999L;
            StudyEnrollRequest request = new StudyEnrollRequest("신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyGeneralService.enrollInStudy(nonExistentStudyId, request, userDetails));
            assertEquals(ErrorCode.STUDY_NOT_FOUND, exception.getErrorCode());
        }
    }

    private Study createStudy(String title, StudyRecruitmentMethod recruitmentMethod) {
        Study study = Study.create(
                title,
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "테스트용 스터디 설명",
                recruitmentMethod,
                10,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        return studyRepository.save(study);
    }

    private Study createStudyWithMaxParticipants(int maxParticipants, StudyRecruitmentMethod recruitmentMethod) {
        Study study = Study.create(
                "정원 제한 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "테스트용 스터디 설명",
                recruitmentMethod,
                maxParticipants,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        return studyRepository.save(study);
    }

    private StudyMember createStudyMember(Study study, Member member, StudyRole role) {
        StudyMember studyMember = StudyMember.create(study, member, role);
        return studyMemberRepository.save(studyMember);
    }

    private StudyApplication createStudyApplication(Study study, Member member, String applicationReason) {
        StudyApplication studyApplication = StudyApplication.create(study, member, applicationReason);
        return studyApplicationRepository.save(studyApplication);
    }

    private StudyCreateUpdateRequest createStudyCreateUpdateRequest() {
        return new StudyCreateUpdateRequest(
                "새로운 스터디",
                StudyCategory.ARTIFICIAL_INTELLIGENCE,
                StudyLevel.ADVANCED,
                "AI 스터디 설명",
                StudyRecruitmentMethod.APPLICATION,
                15,
                "주 3회",
                List.of("AI 커리큘럼 1", "AI 커리큘럼 2"),
                List.of("AI 자격 요건 1", "AI 자격 요건 2"));
    }
}
