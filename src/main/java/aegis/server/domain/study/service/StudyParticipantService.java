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
import aegis.server.domain.point.service.PointLedger;
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
    private final PointLedger pointLedger;
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
        String idempotencyKey = IdempotencyKeys.forStudyAttendance(session.getId(), participantId);
        String reason = String.format("%s 스터디 출석", session.getStudy().getTitle());
        pointLedger.earn(participantId, BigDecimal.valueOf(10), reason, idempotencyKey);

        log.info(
                "[StudyParticipantService] 스터디원 포인트 지급: studyId={}, sessionId={}, participantId={}, amount={}",
                session.getStudy().getId(),
                session.getId(),
                participantId,
                BigDecimal.valueOf(10));
    }

    private void rewardInstructor(StudySession session) {
        StudyMember instructor = studyMemberRepository
                .findFirstByStudyIdAndRole(session.getStudy().getId(), StudyRole.INSTRUCTOR)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_INSTRUCTOR_NOT_FOUND));
        Member instructorMember = instructor.getMember();

        String idempotencyKey = IdempotencyKeys.forStudyInstructor(session.getId(), instructorMember.getId());
        String reason = String.format("%s 스터디 진행", session.getStudy().getTitle());
        pointLedger.earn(instructorMember.getId(), BigDecimal.valueOf(30), reason, idempotencyKey);

        log.info(
                "[StudyParticipantService] 스터디장 포인트 지급: studyId={}, sessionId={}, instructorId={}, amount={}",
                session.getStudy().getId(),
                session.getId(),
                instructorMember.getId(),
                BigDecimal.valueOf(30));
    }
}
