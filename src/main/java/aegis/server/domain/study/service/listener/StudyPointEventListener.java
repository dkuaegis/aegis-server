package aegis.server.domain.study.service.listener;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;
import aegis.server.domain.point.repository.PointAccountRepository;
import aegis.server.domain.point.repository.PointTransactionRepository;
import aegis.server.domain.study.domain.StudyAttendanceReward;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.domain.StudySession;
import aegis.server.domain.study.domain.StudySessionInstructorReward;
import aegis.server.domain.study.domain.event.StudyAttendanceMarkedEvent;
import aegis.server.domain.study.repository.StudyAttendanceRewardRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudySessionInstructorRewardRepository;
import aegis.server.domain.study.repository.StudySessionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyPointEventListener {

    private static final BigDecimal INSTRUCTOR_REWARD_POINT = BigDecimal.valueOf(30);
    private static final BigDecimal PARTICIPANT_REWARD_POINT = BigDecimal.valueOf(10);

    private final StudySessionRepository studySessionRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudySessionInstructorRewardRepository studySessionInstructorRewardRepository;
    private final StudyAttendanceRewardRepository studyAttendanceRewardRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAttendanceMarked(StudyAttendanceMarkedEvent event) {
        Long sessionId = event.sessionId();
        Long participantId = event.participantId();

        StudySession session = studySessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_SESSION_NOT_FOUND));

        // 참가자 10 포인트 지급
        try {
            rewardParticipant(session, participantId);
        } catch (DataIntegrityViolationException ignored) {
        }

        // 스터디장 30 포인트 지급
        StudyMember instructorMember = studyMemberRepository
                .findFirstByStudyIdAndRole(session.getStudy().getId(), StudyRole.INSTRUCTOR)
                .orElse(null);

        if (instructorMember == null) {
            log.error(
                    "[StudyPointEventListener][StudyAttendanceMarkedEvent] 스터디장 미등록으로 보상 생략: studyId={}, sessionId={}",
                    session.getStudy().getId(),
                    sessionId);
            return;
        }

        Member instructor = instructorMember.getMember();
        if (studySessionInstructorRewardRepository.existsByStudySessionIdAndInstructorId(
                sessionId, instructor.getId())) {
            return;
        }

        try {
            rewardInstructor(session, instructor);
        } catch (DataIntegrityViolationException ignored) {
        }
    }

    private void rewardParticipant(StudySession session, Long participantId) {
        if (studyAttendanceRewardRepository.existsByStudySessionIdAndParticipantId(session.getId(), participantId)) {
            return;
        }

        PointAccount account = pointAccountRepository
                .findByIdWithLock(participantId)
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        account.add(PARTICIPANT_REWARD_POINT);

        String reason = String.format("%s 스터디 출석", session.getStudy().getTitle());
        PointTransaction tx =
                PointTransaction.create(account, PointTransactionType.EARN, PARTICIPANT_REWARD_POINT, reason);
        pointTransactionRepository.save(tx);

        studyAttendanceRewardRepository.save(StudyAttendanceReward.create(session, account.getMember(), tx));

        log.info(
                "[StudyPointEventListener][StudyAttendanceMarkedEvent] 스터디원 보상 지급: studyId={}, sessionId={}, participantId={}, amount={}",
                session.getStudy().getId(),
                session.getId(),
                participantId,
                PARTICIPANT_REWARD_POINT);
    }

    private void rewardInstructor(StudySession session, Member instructor) {
        PointAccount account = pointAccountRepository
                .findByIdWithLock(instructor.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.POINT_ACCOUNT_NOT_FOUND));

        account.add(INSTRUCTOR_REWARD_POINT);

        String reason = String.format("%s 스터디 진행", session.getStudy().getTitle());
        PointTransaction tx =
                PointTransaction.create(account, PointTransactionType.EARN, INSTRUCTOR_REWARD_POINT, reason);
        pointTransactionRepository.save(tx);

        studySessionInstructorRewardRepository.save(StudySessionInstructorReward.create(session, instructor, tx));

        log.info(
                "[StudyPointEventListener][StudyAttendanceMarkedEvent] 스터디장 보상 지급: studyId={}, sessionId={}, instructorId={}, name={}, amount={}",
                session.getStudy().getId(),
                session.getId(),
                instructor.getId(),
                instructor.getName(),
                INSTRUCTOR_REWARD_POINT);
    }
}
