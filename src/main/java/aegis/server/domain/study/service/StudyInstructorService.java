package aegis.server.domain.study.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.domain.StudySession;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.dto.response.AttendanceMatrixResponse;
import aegis.server.domain.study.dto.response.AttendanceMemberRow;
import aegis.server.domain.study.dto.response.AttendanceSessionHeader;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationReason;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationSummary;
import aegis.server.domain.study.dto.response.InstructorStudyMemberResponse;
import aegis.server.domain.study.repository.AttendanceMemberSessionPair;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyAttendanceRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.domain.study.repository.StudySessionRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyInstructorService {

    private final StudyRepository studyRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudySessionRepository studySessionRepository;
    private final StudyAttendanceRepository studyAttendanceRepository;
    private final Clock clock;

    private static final char[] CODE_CHARS = "123456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    public List<InstructorStudyApplicationSummary> findAllStudyApplications(Long studyId, UserDetails userDetails) {
        validateIsStudyInstructorByStudyId(studyId, userDetails.getMemberId());

        List<StudyApplication> studyApplications = studyApplicationRepository.findAllByStudyIdWithMember(studyId);
        return studyApplications.stream()
                .map(InstructorStudyApplicationSummary::from)
                .toList();
    }

    public List<InstructorStudyMemberResponse> findAllStudyMembers(Long studyId, UserDetails userDetails) {
        validateIsStudyInstructorByStudyId(studyId, userDetails.getMemberId());

        List<StudyMember> members =
                studyMemberRepository.findByStudyIdAndRoleWithMember(studyId, StudyRole.PARTICIPANT);
        return members.stream()
                .map(sm -> InstructorStudyMemberResponse.from(sm.getMember()))
                .toList();
    }

    public InstructorStudyApplicationReason findStudyApplicationById(
            Long studyId, Long studyApplicationId, UserDetails userDetails) {
        validateIsStudyInstructorByStudyId(studyId, userDetails.getMemberId());

        StudyApplication studyApplication = studyApplicationRepository
                .findById(studyApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_APPLICATION_NOT_FOUND));

        if (!studyApplication.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED);
        }

        return InstructorStudyApplicationReason.from(studyApplication.getApplicationReason());
    }

    @Transactional
    public GeneralStudyDetail updateStudy(Long studyId, StudyCreateUpdateRequest request, UserDetails userDetails) {
        validateIsStudyInstructorByStudyId(studyId, userDetails.getMemberId());

        Study study =
                studyRepository.findById(studyId).orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        study.update(
                request.title(),
                request.category(),
                request.level(),
                request.description(),
                request.recruitmentMethod(),
                request.maxParticipants(),
                request.schedule(),
                request.curricula(),
                request.qualifications());

        return studyRepository.findStudyDetailById(studyId).get();
    }

    @Transactional
    public void approveStudyApplication(Long studyId, Long studyApplicationId, UserDetails userDetails) {
        StudyApplication studyApplication = studyApplicationRepository
                .findByIdWithStudy(studyApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_APPLICATION_NOT_FOUND));

        if (!studyApplication.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED);
        }

        validateIsStudyInstructorByStudyId(studyApplication.getStudy().getId(), userDetails.getMemberId());

        Study study = studyRepository
                .findByIdWithLock(studyApplication.getStudy().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        study.increaseCurrentParticipant();
        studyApplication.approve();

        StudyMember studyMember = StudyMember.create(study, studyApplication.getMember(), StudyRole.PARTICIPANT);
        studyMemberRepository.save(studyMember);
    }

    @Transactional
    public void rejectStudyApplication(Long studyId, Long studyApplicationId, UserDetails userDetails) {
        StudyApplication studyApplication = studyApplicationRepository
                .findByIdWithStudy(studyApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_APPLICATION_NOT_FOUND));

        if (!studyApplication.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_ACCESS_DENIED);
        }

        validateIsStudyInstructorByStudyId(studyApplication.getStudy().getId(), userDetails.getMemberId());

        studyApplication.reject();
    }

    @Transactional
    public AttendanceCodeIssueResponse issueAttendanceCode(Long studyId, UserDetails userDetails) {
        Long requesterId = userDetails.getMemberId();
        validateIsStudyInstructorByStudyId(studyId, requesterId);

        Study study = studyRepository
                .findByIdWithLock(studyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        LocalDate today = LocalDate.now(clock);

        Optional<StudySession> optionalSession =
                studySessionRepository.findByStudyIdAndSessionDate(study.getId(), today);

        if (optionalSession.isPresent()) {
            StudySession existing = optionalSession.get();
            return AttendanceCodeIssueResponse.from(existing.getAttendanceCode(), existing.getId());
        } else {
            String code = generateCode();
            StudySession saved = studySessionRepository.save(StudySession.create(study, today, code));
            return AttendanceCodeIssueResponse.from(code, saved.getId());
        }
    }

    private void validateIsStudyInstructorByStudyId(Long studyId, Long memberId) {
        if (!studyMemberRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, StudyRole.INSTRUCTOR)) {
            throw new CustomException(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR);
        }
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CODE_CHARS[RANDOM.nextInt(CODE_CHARS.length)]);
        }
        return sb.toString();
    }

    public AttendanceMatrixResponse findAttendanceMatrix(Long studyId, UserDetails userDetails) {
        validateIsStudyInstructorByStudyId(studyId, userDetails.getMemberId());

        // 세션 목록 조회
        List<StudySession> sessions = studySessionRepository.findAllByStudyIdOrderBySessionDateAsc(studyId);
        int n = sessions.size();

        // 세션 ID -> 열 인덱스 맵, 세션 헤더 목록 생성
        Map<Long, Integer> sessionIndexMap = new HashMap<>();
        List<AttendanceSessionHeader> sessionHeaders = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            StudySession ss = sessions.get(i);
            sessionIndexMap.put(ss.getId(), i);
            sessionHeaders.add(AttendanceSessionHeader.from(ss.getId(), ss.getSessionDate()));
        }

        // 참가자 목록 조회
        List<StudyMember> participants =
                studyMemberRepository.findByStudyIdAndRoleWithMember(studyId, StudyRole.PARTICIPANT);

        // 참가자 ID -> 출석 배열 맵 초기화
        Map<Long, boolean[]> attendanceMap = new HashMap<>();
        for (StudyMember sm : participants) {
            attendanceMap.put(sm.getMember().getId(), new boolean[n]);
        }

        // 출석 페어 적용
        List<AttendanceMemberSessionPair> pairs = studyAttendanceRepository.findMemberSessionPairsByStudyId(studyId);
        for (AttendanceMemberSessionPair pair : pairs) {
            Integer col = sessionIndexMap.get(pair.getSessionId());
            boolean[] row = attendanceMap.get(pair.getMemberId());
            if (col != null && row != null) {
                row[col] = true;
            }
        }

        // 출석 행렬 생성
        List<AttendanceMemberRow> memberRows = new ArrayList<>(participants.size());
        for (StudyMember sm : participants) {
            Long memberId = sm.getMember().getId();
            String name = sm.getMember().getName();
            boolean[] row = attendanceMap.get(memberId);
            List<Boolean> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) list.add(row != null && row[i]);
            memberRows.add(AttendanceMemberRow.from(memberId, name, list));
        }

        return AttendanceMatrixResponse.from(sessionHeaders, memberRows);
    }
}
