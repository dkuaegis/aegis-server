package aegis.server.domain.study.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.AttendanceMarkRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.dto.response.AttendanceMarkResponse;
import aegis.server.domain.study.repository.StudyAttendanceRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

class StudyParticipantServiceTest extends IntegrationTest {

    @Autowired
    StudyParticipantService studyParticipantService;

    @Autowired
    StudyInstructorService studyInstructorService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    StudyAttendanceRepository studyAttendanceRepository;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setupClock() {
        // 2025-09-11 Asia/Seoul 기준 날짜로 고정
        org.mockito.BDDMockito.given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        org.mockito.BDDMockito.given(clock.instant()).willReturn(Instant.parse("2025-09-11T01:00:00Z"));
    }

    @Nested
    class 출석_성공 {
        @Test
        void 참여자가_정상_코드로_출석한다() {
            // given
            Member instructor = createMember();
            Member participant = createMember();
            UserDetails participantDetails = createUserDetails(participant);

            Study study = createStudyWithInstructor(instructor);
            addParticipant(study, participant);

            // 스터디장이 코드 발급
            AttendanceCodeIssueResponse issued =
                    studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

            // when
            AttendanceMarkResponse response = studyParticipantService.markAttendance(
                    study.getId(), AttendanceMarkRequest.of(issued.code()), participantDetails);

            // then - 반환값
            assertNotNull(response);
            assertNotNull(response.attendanceId());
            assertEquals(issued.sessionId(), response.sessionId());

            // then - DB 상태
            assertTrue(studyAttendanceRepository.existsByStudySessionIdAndMemberId(
                    issued.sessionId(), participant.getId()));
        }
    }

    @Nested
    class 출석_실패 {
        @Test
        void 코드가_일치하지_않으면_실패한다() {
            // given
            Member instructor = createMember();
            Member participant = createMember();
            UserDetails participantDetails = createUserDetails(participant);

            Study study = createStudyWithInstructor(instructor);
            addParticipant(study, participant);
            studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

            // when & then
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyParticipantService.markAttendance(
                            study.getId(), AttendanceMarkRequest.of("9999"), participantDetails));
            assertEquals(ErrorCode.STUDY_ATTENDANCE_CODE_INVALID, e.getErrorCode());
        }

        @Test
        void 오늘_세션이_없으면_실패한다() {
            // given
            Member instructor = createMember();
            Member participant = createMember();
            UserDetails participantDetails = createUserDetails(participant);

            Study study = createStudyWithInstructor(instructor);
            addParticipant(study, participant);
            // 세션 미발급

            // when & then
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyParticipantService.markAttendance(
                            study.getId(), AttendanceMarkRequest.of("1234"), participantDetails));
            assertEquals(ErrorCode.STUDY_SESSION_NOT_FOUND, e.getErrorCode());
        }

        @Test
        void 스터디원이_아니면_실패한다() {
            // given
            Member instructor = createMember();
            Member notParticipant = createMember();
            UserDetails notParticipantDetails = createUserDetails(notParticipant);

            Study study = createStudyWithInstructor(instructor);
            AttendanceCodeIssueResponse issued =
                    studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

            // when & then
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyParticipantService.markAttendance(
                            study.getId(), AttendanceMarkRequest.of(issued.code()), notParticipantDetails));
            assertEquals(ErrorCode.STUDY_MEMBER_NOT_PARTICIPANT, e.getErrorCode());
        }

        @Test
        void 이미_출석한_경우_실패한다() {
            // given
            Member instructor = createMember();
            Member participant = createMember();
            UserDetails participantDetails = createUserDetails(participant);

            Study study = createStudyWithInstructor(instructor);
            addParticipant(study, participant);
            AttendanceCodeIssueResponse issued =
                    studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

            // 1차 출석 성공
            studyParticipantService.markAttendance(
                    study.getId(), AttendanceMarkRequest.of(issued.code()), participantDetails);

            // when & then - 2차 출석 시도
            CustomException e = assertThrows(
                    CustomException.class,
                    () -> studyParticipantService.markAttendance(
                            study.getId(), AttendanceMarkRequest.of(issued.code()), participantDetails));
            assertEquals(ErrorCode.STUDY_ATTENDANCE_ALREADY_MARKED, e.getErrorCode());
        }
    }

    private Study createStudyWithInstructor(Member instructor) {
        Study study = Study.create(
                "출석 스터디",
                StudyCategory.WEB,
                StudyLevel.BASIC,
                "출석 테스트용",
                StudyRecruitmentMethod.APPLICATION,
                10,
                "주 1회",
                java.util.List.of("커리큘럼1"),
                java.util.List.of("자격1"));
        study = studyRepository.save(study);

        studyMemberRepository.save(StudyMember.create(study, instructor, StudyRole.INSTRUCTOR));
        return study;
    }

    private void addParticipant(Study study, Member participant) {
        studyMemberRepository.save(StudyMember.create(study, participant, StudyRole.PARTICIPANT));
    }
}
