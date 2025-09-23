package aegis.server.domain.study.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.common.idempotency.IdempotencyKeys;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.study.domain.StudyAttendance;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.domain.StudySession;
import aegis.server.domain.study.dto.request.AttendanceMarkRequest;
import aegis.server.domain.study.dto.response.AttendanceMarkResponse;
import aegis.server.domain.study.repository.StudyAttendanceRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudySessionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyParticipantService {

    private final MemberRepository memberRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudySessionRepository studySessionRepository;
    private final StudyAttendanceRepository studyAttendanceRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final Clock clock;

    @Transactional
    public AttendanceMarkResponse markAttendance(Long studyId, AttendanceMarkRequest request, UserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        Member member =
                memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 참여자 권한 검증
        validateIsStudyParticipantByStudyId(studyId, memberId);

        // 오늘 세션 조회
        LocalDate today = LocalDate.now(clock);
        StudySession session = studySessionRepository
                .findByStudyIdAndSessionDate(studyId, today)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_SESSION_NOT_FOUND));

        // 코드 검증
        if (!session.getAttendanceCode().equals(request.code())) {
            throw new CustomException(ErrorCode.STUDY_ATTENDANCE_CODE_INVALID);
        }

        try {
            StudyAttendance saved = studyAttendanceRepository.save(StudyAttendance.create(session, member));
            log.info(
                    "[StudyParticipantService] 출석 완료: studyId={}, sessionId={}, attendanceId={}, memberId={}, memberName={}",
                    studyId,
                    session.getId(),
                    saved.getId(),
                    member.getId(),
                    member.getName());

            // 포인트 보상: 참가자 + 스터디장
            rewardParticipant(session, memberId);
            rewardInstructor(session);

            return AttendanceMarkResponse.from(saved.getId(), session.getId());
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.STUDY_ATTENDANCE_ALREADY_MARKED);
        }
    }

    private void validateIsStudyParticipantByStudyId(Long studyId, Long memberId) {
        if (!studyMemberRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.PARTICIPANT)) {
            throw new CustomException(ErrorCode.STUDY_MEMBER_NOT_PARTICIPANT);
        }
    }

    private void rewardParticipant(StudySession session, Long participantId) {
        // 1. 계좌 잔고 증가
        PointAccount account = pointAccountRepository
                .findByIdWithLock(participantId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));
        String idempotencyKey = IdempotencyKeys.forStudyAttendance(session.getId(), participantId);
        if (pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)) return;
        account.add(BigDecimal.valueOf(10));

        // 2. 트랜잭션 기록
        String reason = String.format("%s 스터디 출석", session.getStudy().getTitle());
        PointTransaction pointTransaction = PointTransaction.create(
                account, PointTransactionType.EARN, BigDecimal.valueOf(10), reason, idempotencyKey);
        pointTransactionRepository.save(pointTransaction);

        log.info(
                "[StudyParticipantService] 스터디원 포인트 지급: studyId={}, sessionId={}, participantId={}, amount={}",
                session.getStudy().getId(),
                session.getId(),
                participantId,
                BigDecimal.valueOf(10));
    }

    private void rewardInstructor(StudySession session) {
        StudyMember instructorMember = studyMemberRepository
                .findFirstByStudyIdAndRole(session.getStudy().getId(), StudyRole.INSTRUCTOR)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_INSTRUCTOR_NOT_FOUND));
        Member instructor = instructorMember.getMember();

        // 1. 계좌 잔고 증가
        PointAccount account = pointAccountRepository
                .findByIdWithLock(instructor.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));
        String idempotencyKey = IdempotencyKeys.forStudyInstructor(session.getId(), instructor.getId());
        if (pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)) return;
        account.add(BigDecimal.valueOf(30));

        // 2. 트랜잭션 기록
        String reason = String.format("%s 스터디 진행", session.getStudy().getTitle());
        PointTransaction pointTransaction = PointTransaction.create(
                account, PointTransactionType.EARN, BigDecimal.valueOf(30), reason, idempotencyKey);
        pointTransactionRepository.save(pointTransaction);

        log.info(
                "[StudyParticipantService] 스터디장 포인트 지급: studyId={}, sessionId={}, instructorId={}, name={}, amount={}",
                session.getStudy().getId(),
                session.getId(),
                instructor.getId(),
                instructor.getName(),
                BigDecimal.valueOf(30));
    }
}
