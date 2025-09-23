package aegis.server.domain.study.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.AttendanceMarkRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.repository.StudyAttendanceRewardRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.domain.study.repository.StudySessionInstructorRewardRepository;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class StudyPointRewardTest extends IntegrationTestWithoutTransactional {

    @Autowired
    StudyParticipantService studyParticipantService;

    @Autowired
    StudyInstructorService studyInstructorService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    StudySessionInstructorRewardRepository sessionRewardRepository;

    @Autowired
    StudyAttendanceRewardRepository attendanceRewardRepository;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setupClock() {
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        given(clock.instant()).willReturn(Instant.parse("2025-09-11T01:00:00Z"));
    }

    @Test
    void 첫_출석_발생_시_스터디장_30포인트_지급된다() {
        // given
        Member instructor = createMember();
        Member participant = createMember();

        PointAccount instructorAccount = pointAccountRepository.save(PointAccount.create(instructor));
        pointAccountRepository.save(PointAccount.create(participant));
        BigDecimal before = instructorAccount.getBalance();

        Study study = createStudyWithInstructor(instructor);
        addParticipant(study, participant);

        AttendanceCodeIssueResponse issued =
                studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

        // when: 첫 출석
        studyParticipantService.markAttendance(
                study.getId(), AttendanceMarkRequest.of(issued.code()), createUserDetails(participant));

        // then: 스터디장 보상 1회 지급
        PointAccount refreshed =
                pointAccountRepository.findByMemberId(instructor.getId()).orElseThrow();
        assertEquals(before.add(BigDecimal.valueOf(30)), refreshed.getBalance());
        assertTrue(
                sessionRewardRepository.existsByStudySessionIdAndInstructorId(issued.sessionId(), instructor.getId()));
    }

    @Test
    void 여러_참가자_출석해도_스터디장_보상은_세션당_1회만_지급() {
        // given
        Member instructor = createMember();
        Member p1 = createMember();
        Member p2 = createMember();
        pointAccountRepository.save(PointAccount.create(instructor));
        pointAccountRepository.save(PointAccount.create(p1));
        pointAccountRepository.save(PointAccount.create(p2));

        Study study = createStudyWithInstructor(instructor);
        addParticipant(study, p1);
        addParticipant(study, p2);

        AttendanceCodeIssueResponse issued =
                studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

        // when: 두 명이 순차로 출석
        studyParticipantService.markAttendance(
                study.getId(), AttendanceMarkRequest.of(issued.code()), createUserDetails(p1));
        studyParticipantService.markAttendance(
                study.getId(), AttendanceMarkRequest.of(issued.code()), createUserDetails(p2));

        // then: 보상은 1건만
        assertTrue(
                sessionRewardRepository.existsByStudySessionIdAndInstructorId(issued.sessionId(), instructor.getId()));
        long rewardCount = sessionRewardRepository.findAll().stream()
                .filter(r -> r.getStudySession().getId().equals(issued.sessionId()))
                .count();
        assertEquals(1L, rewardCount);
    }

    @Test
    void 출석_성공_시_참가자_10포인트_지급() {
        // given
        Member instructor = createMember();
        Member participant = createMember();
        pointAccountRepository.save(PointAccount.create(instructor));
        PointAccount participantAccount = pointAccountRepository.save(PointAccount.create(participant));
        BigDecimal before = participantAccount.getBalance();

        Study study = createStudyWithInstructor(instructor);
        addParticipant(study, participant);

        AttendanceCodeIssueResponse issued =
                studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

        // when
        studyParticipantService.markAttendance(
                study.getId(), AttendanceMarkRequest.of(issued.code()), createUserDetails(participant));

        // then
        PointAccount refreshed =
                pointAccountRepository.findByMemberId(participant.getId()).orElseThrow();
        assertEquals(before.add(BigDecimal.valueOf(10)), refreshed.getBalance());
        assertTrue(attendanceRewardRepository.existsByStudySessionIdAndParticipantId(
                issued.sessionId(), participant.getId()));
    }

    private Study createStudyWithInstructor(Member instructor) {
        Study study = Study.create(
                "포인트 보상 스터디",
                StudyCategory.WEB,
                StudyLevel.BASIC,
                "보상 테스트",
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
