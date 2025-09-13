package aegis.server.domain.study.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationReason;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationSummary;
import aegis.server.domain.study.dto.response.InstructorStudyMemberResponse;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.domain.study.repository.StudySessionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class StudyInstructorServiceTest extends IntegrationTest {

    @Autowired
    StudyInstructorService studyInstructorService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    StudyApplicationRepository studyApplicationRepository;

    @Autowired
    StudySessionRepository studySessionRepository;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setupClock() {
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        given(clock.instant()).willReturn(Instant.parse("2025-09-11T01:00:00Z"));
    }

    @Nested
    class 스터디_신청_목록_조회 {

        @Test
        void 강사가_자신의_스터디_신청_목록을_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant1 = createMember();
            Member applicant2 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            createStudyApplication(study, applicant1, "신청 사유1");
            createStudyApplication(study, applicant2, "신청 사유2");

            // when
            List<InstructorStudyApplicationSummary> response =
                    studyInstructorService.findAllStudyApplications(study.getId(), instructorDetails);

            // then
            assertEquals(2, response.size());
            assertTrue(response.stream().anyMatch(summary -> summary.name().equals(applicant1.getName())));
            assertTrue(response.stream().anyMatch(summary -> summary.name().equals(applicant2.getName())));
        }

        @Test
        void 신청이_없는_경우_빈_리스트를_반환한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            List<InstructorStudyApplicationSummary> response =
                    studyInstructorService.findAllStudyApplications(study.getId(), instructorDetails);

            // then
            assertEquals(0, response.size());
        }

        @Test
        void 강사가_아닌_사용자가_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findAllStudyApplications(study.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }

    @Nested
    class 출석_코드_발급 {

        @Test
        void 스터디장이_오늘의_세션에_대한_코드를_발급한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            AttendanceCodeIssueResponse response =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // then
            // 반환값 검증
            assertNotNull(response);
            assertNotNull(response.code());
            assertEquals(4, response.code().length());
            assertNotNull(response.sessionId());

            // DB 상태 검증
            StudySession session =
                    studySessionRepository.findById(response.sessionId()).get();
            assertNotNull(session);
            assertEquals(LocalDate.now(clock), session.getSessionDate());
            assertEquals(response.code(), session.getAttendanceCode());
        }

        @Test
        void 같은_날_재발급하면_세션과_코드는_유지된다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            AttendanceCodeIssueResponse first =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // when
            AttendanceCodeIssueResponse second =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // then
            // 반환값 검증
            assertEquals(first.sessionId(), second.sessionId());
            assertEquals(first.code(), second.code());

            // DB 상태 검증
            StudySession session =
                    studySessionRepository.findById(first.sessionId()).get();
            assertEquals(first.code(), session.getAttendanceCode());
            assertEquals(second.code(), session.getAttendanceCode());
        }

        @Test
        void 스터디장이_아니면_발급할_수_없다() {
            // given
            Member instructor = createMember();
            Member notInstructor = createMember();
            UserDetails notInstructorDetails = createUserDetails(notInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.issueAttendanceCode(study.getId(), notInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, e.getErrorCode());
        }

        @Test
        void 날짜가_바뀌면_새로운_세션이_생성된다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when: 순차 호출마다 다른 날짜가 되도록 스텁
            given(clock.instant())
                    .willReturn(Instant.parse("2025-09-11T10:00:00Z"), Instant.parse("2025-09-12T10:00:00Z"));

            AttendanceCodeIssueResponse first =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            AttendanceCodeIssueResponse second =
                    studyInstructorService.issueAttendanceCode(study.getId(), instructorDetails);

            // then
            assertNotEquals(first.sessionId(), second.sessionId());
        }
    }

    @Nested
    class 스터디원_목록_조회 {

        @Test
        void 강사가_자신의_스터디원_목록을_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member participant1 = createMember();
            Member participant2 = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);

            // 참여자 등록
            studyMemberRepository.save(StudyMember.create(study, participant1, StudyRole.PARTICIPANT));
            studyMemberRepository.save(StudyMember.create(study, participant2, StudyRole.PARTICIPANT));

            // when
            List<InstructorStudyMemberResponse> response =
                    studyInstructorService.findAllStudyMembers(study.getId(), instructorDetails);

            // then
            assertEquals(2, response.size());
            assertTrue(response.stream().anyMatch(r -> r.name().equals(participant1.getName())));
            assertTrue(response.stream().anyMatch(r -> r.name().equals(participant2.getName())));
            assertTrue(response.stream().allMatch(r -> r.phoneNumber() != null));
            assertTrue(response.stream().allMatch(r -> r.studentId() != null));
        }

        @Test
        void 스터디원이_없으면_빈_리스트를_반환한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);

            // when
            List<InstructorStudyMemberResponse> response =
                    studyInstructorService.findAllStudyMembers(study.getId(), instructorDetails);

            // then
            assertEquals(0, response.size());
        }

        @Test
        void 강사가_아닌_사용자가_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);
            Study study = createStudyWithInstructor(instructor);

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findAllStudyMembers(study.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_신청_상세_조회 {

        @Test
        void 강사가_자신의_스터디_신청_상세를_조회할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "상세 신청 사유입니다");

            // when
            InstructorStudyApplicationReason response = studyInstructorService.findStudyApplicationById(
                    study.getId(), application.getId(), instructorDetails);

            // then
            assertEquals("상세 신청 사유입니다", response.applicationReason());
        }

        @Test
        void 강사가_아닌_사용자가_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findStudyApplicationById(
                            study.getId(), application.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_신청을_조회하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            Long nonExistentApplicationId = 999L;

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findStudyApplicationById(
                            study.getId(), nonExistentApplicationId, instructorDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_스터디의_신청을_조회하면_예외가_발생한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Member applicant = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);
            StudyApplication application = createStudyApplication(study2, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.findStudyApplicationById(
                            study1.getId(), application.getId(), instructor1Details));
            assertEquals(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_수정 {

        @Test
        void 강사가_자신의_스터디를_수정할_수_있다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when
            GeneralStudyDetail response = studyInstructorService.updateStudy(study.getId(), request, instructorDetails);

            // then
            // 반환값 검증
            assertEquals(request.title(), response.title());
            assertEquals(request.category(), response.category());
            assertEquals(request.level(), response.level());
            assertEquals(request.description(), response.description());
            assertEquals(request.recruitmentMethod(), response.recruitmentMethod());

            // DB 상태 검증
            Study updatedStudy = studyRepository.findById(study.getId()).get();
            assertEquals(request.title(), updatedStudy.getTitle());
            assertEquals(request.category(), updatedStudy.getCategory());
            assertEquals(request.level(), updatedStudy.getLevel());
        }

        @Test
        void 강사가_아닌_사용자가_수정하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);
            Study study = createStudyWithInstructor(instructor);
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.updateStudy(study.getId(), request, nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_스터디를_수정하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Long nonExistentStudyId = 999L;
            StudyCreateUpdateRequest request = createStudyCreateUpdateRequest();

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.updateStudy(nonExistentStudyId, request, instructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }
    }

    @Nested
    class 스터디_신청_승인 {

        @Test
        void 강사가_신청을_승인할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when
            studyInstructorService.approveStudyApplication(study.getId(), application.getId(), instructorDetails);

            // then
            // StudyApplication 승인 상태로 변경 검증
            StudyApplication updatedApplication =
                    studyApplicationRepository.findById(application.getId()).get();
            assertEquals(StudyApplicationStatus.APPROVED, updatedApplication.getStatus());

            // StudyMember 생성 검증
            assertTrue(
                    studyMemberRepository.findByStudyAndMember(study, applicant).isPresent());
            StudyMember studyMember =
                    studyMemberRepository.findByStudyAndMember(study, applicant).get();
            assertEquals(StudyRole.PARTICIPANT, studyMember.getRole());

            // currentParticipants 증가 검증
            Study updatedStudy = studyRepository.findById(study.getId()).get();
            assertEquals(1, updatedStudy.getCurrentParticipants());
        }

        @Test
        void 강사가_아닌_사용자가_승인하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study.getId(), application.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_신청을_승인하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            Long nonExistentApplicationId = 999L;

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study.getId(), nonExistentApplicationId, instructorDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_스터디의_신청을_승인하면_예외가_발생한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Member applicant = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);
            StudyApplication application = createStudyApplication(study2, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study1.getId(), application.getId(), instructor1Details));
            assertEquals(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED, exception.getErrorCode());
        }

        @Test
        void 정원이_가득_찬_스터디에_승인하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithMaxParticipants(instructor, 1);
            study.increaseCurrentParticipant(); // 정원 가득 채움
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.approveStudyApplication(
                            study.getId(), application.getId(), instructorDetails));
            assertEquals(ErrorCode.STUDY_FULL, exception.getErrorCode());
        }

        @Test
        void 정원이_무제한인_스터디는_승인을_무제한으로_허용한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            int approveCount = 4;
            Study study = createStudyWithMaxParticipants(instructor, 0);

            // 신청서 생성
            Member[] applicants = new Member[approveCount];
            for (int i = 0; i < approveCount; i++) {
                applicants[i] = createMember();
                createStudyApplication(study, applicants[i], "무제한 승인 테스트" + i);
            }

            // when
            List<StudyApplication> applications = studyApplicationRepository.findAllByStudyIdWithMember(study.getId());
            applications.forEach(app ->
                    studyInstructorService.approveStudyApplication(study.getId(), app.getId(), instructorDetails));

            // then
            Study updated = studyRepository.findById(study.getId()).get();
            assertEquals(approveCount, updated.getCurrentParticipants());
        }
    }

    @Nested
    class 스터디_신청_거절 {

        @Test
        void 강사가_신청을_거절할_수_있다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when
            studyInstructorService.rejectStudyApplication(study.getId(), application.getId(), instructorDetails);

            // then
            // StudyApplication 거절 상태로 변경 검증
            StudyApplication updatedApplication =
                    studyApplicationRepository.findById(application.getId()).get();
            assertEquals(StudyApplicationStatus.REJECTED, updatedApplication.getStatus());
        }

        @Test
        void 강사가_아닌_사용자가_거절하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            Member applicant = createMember();
            Member nonInstructor = createMember();
            UserDetails nonInstructorDetails = createUserDetails(nonInstructor);

            Study study = createStudyWithInstructor(instructor);
            StudyApplication application = createStudyApplication(study, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.rejectStudyApplication(
                            study.getId(), application.getId(), nonInstructorDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR, exception.getErrorCode());
        }

        @Test
        void 존재하지_않는_신청을_거절하면_예외가_발생한다() {
            // given
            Member instructor = createMember();
            UserDetails instructorDetails = createUserDetails(instructor);
            Study study = createStudyWithInstructor(instructor);
            Long nonExistentApplicationId = 999L;

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.rejectStudyApplication(
                            study.getId(), nonExistentApplicationId, instructorDetails));
            assertEquals(ErrorCode.STUDY_APPLICATION_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        void 다른_스터디의_신청을_거절하면_예외가_발생한다() {
            // given
            Member instructor1 = createMember();
            Member instructor2 = createMember();
            Member applicant = createMember();
            UserDetails instructor1Details = createUserDetails(instructor1);

            Study study1 = createStudyWithInstructor(instructor1);
            Study study2 = createStudyWithInstructor(instructor2);
            StudyApplication application = createStudyApplication(study2, applicant, "신청 사유");

            // when & then
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> studyInstructorService.rejectStudyApplication(
                            study1.getId(), application.getId(), instructor1Details));
            assertEquals(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED, exception.getErrorCode());
        }
    }

    private Study createStudyWithInstructor(Member instructor) {
        Study study = Study.create(
                "강사 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "강사가 있는 스터디 설명",
                StudyRecruitmentMethod.APPLICATION,
                10,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        study = studyRepository.save(study);

        StudyMember studyMember = StudyMember.create(study, instructor, StudyRole.INSTRUCTOR);
        studyMemberRepository.save(studyMember);

        return study;
    }

    private Study createStudyWithMaxParticipants(Member instructor, int maxParticipants) {
        Study study = Study.create(
                "정원 제한 스터디",
                StudyCategory.WEB,
                StudyLevel.INTERMEDIATE,
                "정원 제한 스터디 설명",
                StudyRecruitmentMethod.APPLICATION,
                maxParticipants,
                "주 2회",
                List.of("테스트 커리큘럼 1", "테스트 커리큘럼 2"),
                List.of("테스트 자격 요건 1", "테스트 자격 요건 2"));
        study = studyRepository.save(study);

        StudyMember studyMember = StudyMember.create(study, instructor, StudyRole.INSTRUCTOR);
        studyMemberRepository.save(studyMember);

        return study;
    }

    private StudyApplication createStudyApplication(Study study, Member member, String applicationReason) {
        StudyApplication studyApplication = StudyApplication.create(study, member, applicationReason);
        return studyApplicationRepository.save(studyApplication);
    }

    private StudyCreateUpdateRequest createStudyCreateUpdateRequest() {
        return new StudyCreateUpdateRequest(
                "수정된 스터디",
                StudyCategory.ARTIFICIAL_INTELLIGENCE,
                StudyLevel.ADVANCED,
                "수정된 스터디 설명",
                StudyRecruitmentMethod.FCFS,
                20,
                "주 3회",
                List.of("수정된 커리큘럼 1", "수정된 커리큘럼 2"),
                List.of("수정된 자격 요건 1", "수정된 자격 요건 2"));
    }
}
