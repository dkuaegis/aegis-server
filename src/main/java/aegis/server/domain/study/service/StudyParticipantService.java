package aegis.server.domain.study.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.study.domain.StudyAttendance;
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

        // 중복 방지 선검증
        if (studyAttendanceRepository.existsByStudySessionIdAndMemberId(session.getId(), memberId)) {
            throw new CustomException(ErrorCode.STUDY_ATTENDANCE_ALREADY_MARKED);
        }

        try {
            StudyAttendance saved = studyAttendanceRepository.save(StudyAttendance.create(session, member));
            log.info(
                    "[Study][Attendance] 출석 완료: studyId={}, sessionId={}, attendanceId={}, memberId={}, memberName={}",
                    studyId,
                    session.getId(),
                    saved.getId(),
                    member.getId(),
                    member.getName());
            return AttendanceMarkResponse.from(saved.getId(), session.getId());
        } catch (DataIntegrityViolationException e) {
            // 경쟁 조건으로 인한 중복 방지
            throw new CustomException(ErrorCode.STUDY_ATTENDANCE_ALREADY_MARKED);
        }
    }

    private void validateIsStudyParticipantByStudyId(Long studyId, Long memberId) {
        if (!studyMemberRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.PARTICIPANT)) {
            throw new CustomException(ErrorCode.STUDY_MEMBER_NOT_PARTICIPANT);
        }
    }
}
