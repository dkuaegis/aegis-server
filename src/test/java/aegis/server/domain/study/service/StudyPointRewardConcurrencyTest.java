package aegis.server.domain.study.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import aegis.server.domain.common.idempotency.IdempotencyKeys;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.AttendanceMarkRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.repository.StudyAttendanceRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.helper.IntegrationTestWithoutTransactional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("concurrency")
@ActiveProfiles("postgres")
class StudyPointRewardConcurrencyTest extends IntegrationTestWithoutTransactional {

    @Autowired
    StudyParticipantService studyParticipantService;

    @Autowired
    StudyInstructorService studyInstructorService;

    @Autowired
    StudyRepository studyRepository;

    @Autowired
    StudyMemberRepository studyMemberRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Autowired
    PointAccountRepository pointAccountRepository;

    @Autowired
    StudyAttendanceRepository studyAttendanceRepository;

    @MockitoBean
    Clock clock;

    @BeforeEach
    void setupClock() {
        // 고정된 오늘 날짜로 세션 발급/조회 일치
        org.mockito.BDDMockito.given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        org.mockito.BDDMockito.given(clock.instant()).willReturn(Instant.parse("2025-09-11T01:00:00Z"));
    }

    @Test
    void 다수가_동시_출석에도_스터디장_보상은_정확히_1회만_지급된다() throws Exception {
        // given
        int participantCount = 50;

        Member instructor = createMember();
        PointAccount instructorAccount = pointAccountRepository.save(PointAccount.create(instructor));
        BigDecimal beforeInstructor = instructorAccount.getBalance();

        List<Member> participants = new ArrayList<>(participantCount);
        for (int i = 0; i < participantCount; i++) {
            Member p = createMember();
            participants.add(p);
            pointAccountRepository.save(PointAccount.create(p));
        }

        Study study = createStudyWithInstructor(instructor);
        for (Member p : participants) {
            addParticipant(study, p);
        }

        AttendanceCodeIssueResponse issued =
                studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

        // when: 참가자들이 동시에 출석 처리
        ExecutorService pool = Executors.newFixedThreadPool(participantCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(participantCount);

        List<CompletableFuture<Void>> futures = new ArrayList<>(participantCount);
        for (Member p : participants) {
            futures.add(CompletableFuture.runAsync(
                    () -> {
                        try {
                            start.await();
                            studyParticipantService.markAttendance(
                                    study.getId(), AttendanceMarkRequest.of(issued.code()), createUserDetails(p));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            end.countDown();
                        }
                    },
                    pool));
        }
        start.countDown();
        end.await();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        pool.shutdown();

        // then: 스터디장 보상 1회만 적립됨
        PointAccount refreshed =
                pointAccountRepository.findByMemberId(instructor.getId()).orElseThrow();
        assertEquals(beforeInstructor.add(BigDecimal.valueOf(30)), refreshed.getBalance());
        String instructorKey = IdempotencyKeys.forStudyInstructor(issued.sessionId(), instructor.getId());
        assertTrue(pointTransactionRepository.existsByIdempotencyKey(instructorKey));
    }

    @Test
    void 동일_스터디원이_동시에_출석해도_포인트는_1회만_지급된다() throws Exception {
        // given
        int attempts = 50;

        Member instructor = createMember();
        Member participant = createMember();

        PointAccount instructorAccount = pointAccountRepository.save(PointAccount.create(instructor));
        PointAccount participantAccount = pointAccountRepository.save(PointAccount.create(participant));

        BigDecimal beforeParticipant = participantAccount.getBalance();

        Study study = createStudyWithInstructor(instructor);
        addParticipant(study, participant);

        AttendanceCodeIssueResponse issued =
                studyInstructorService.issueAttendanceCode(study.getId(), createUserDetails(instructor));

        // when: 동일한 스터디원이 여러 단말에서 동시에 출석 요청
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(attempts);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>(attempts);
        for (int i = 0; i < attempts; i++) {
            futures.add(CompletableFuture.runAsync(
                    () -> {
                        try {
                            start.await();
                            studyParticipantService.markAttendance(
                                    study.getId(),
                                    AttendanceMarkRequest.of(issued.code()),
                                    createUserDetails(participant));
                            success.incrementAndGet();
                        } catch (CustomException e) {
                            if (e.getErrorCode() == ErrorCode.STUDY_ATTENDANCE_ALREADY_MARKED) {
                                conflicts.incrementAndGet();
                            } else {
                                throw new RuntimeException(e);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            end.countDown();
                        }
                    },
                    pool));
        }

        start.countDown();
        end.await();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        pool.shutdown();

        // then: 출석은 1회만, 포인트도 1회(10점)만 적립됨
        assertEquals(1, success.get());
        assertEquals(attempts - 1, conflicts.get());

        // 출석 1회 보장 (유니크 제약)
        assertTrue(
                studyAttendanceRepository.existsByStudySessionIdAndMemberId(issued.sessionId(), participant.getId()));

        // 포인트 1회 적립 확인 (잔액 +10, 멱등키 기록 존재)
        PointAccount refreshedParticipant =
                pointAccountRepository.findByMemberId(participant.getId()).orElseThrow();
        assertEquals(beforeParticipant.add(BigDecimal.valueOf(10)), refreshedParticipant.getBalance());
        String participantKey = IdempotencyKeys.forStudyAttendance(issued.sessionId(), participant.getId());
        assertTrue(pointTransactionRepository.existsByIdempotencyKey(participantKey));
    }

    private Study createStudyWithInstructor(Member instructor) {
        Study study = Study.create(
                "동시성 보상 테스트",
                StudyCategory.WEB,
                StudyLevel.BASIC,
                "보상 동시성",
                StudyRecruitmentMethod.APPLICATION,
                500,
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
