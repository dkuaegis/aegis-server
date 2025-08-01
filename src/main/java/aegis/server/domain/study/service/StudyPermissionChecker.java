package aegis.server.domain.study.service;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Role;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Component
@RequiredArgsConstructor
public class StudyPermissionChecker {

    private final StudyMemberRepository studyMemberRepository;

    /**
     * 스터디에 참여 중인 사용자인지 검증합니다.
     */
    public void validateIsParticipant(Long studyId, UserDetails userDetails) {
        if (!existsByStudyIdAndMemberIdAndRole(studyId, userDetails.getMemberId(), StudyRole.PARTICIPANT)) {
            throw new CustomException(ErrorCode.STUDY_MEMBER_NOT_PARTICIPANT);
        }
    }

    /**
     * 스터디장이거나 관리자 권한을 가진 사용자인지 검증합니다.
     */
    public void validateIsInstructor(Long studyId, UserDetails userDetails) {
        if (!userDetails.getRole().equals(Role.ADMIN)
                && !existsByStudyIdAndMemberIdAndRole(studyId, userDetails.getMemberId(), StudyRole.INSTRUCTOR)) {
            throw new CustomException(ErrorCode.STUDY_MEMBER_NOT_INSTRUCTOR);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole role) {
        return studyMemberRepository.existsByStudyIdAndMemberIdAndRole(studyId, memberId, role);
    }
}
