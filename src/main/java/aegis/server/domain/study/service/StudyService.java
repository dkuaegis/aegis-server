package aegis.server.domain.study.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyPermissionChecker studyPermissionChecker;

    @Transactional
    public void createStudy(StudyCreateUpdateRequest request, UserDetails userDetails) {
        Member member = memberRepository
                .findById(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Study study = studyRepository.save(Study.create(
                request.title(),
                request.category(),
                request.level(),
                request.description(),
                request.recruitmentMethod(),
                request.maxParticipants(),
                request.schedule(),
                request.curricula(),
                request.qualifications()));

        // 스터디 생성한 사람을 스터디장으로 등록
        studyMemberRepository.save(StudyMember.create(study, member, StudyRole.INSTRUCTOR));
    }

    @Transactional
    public void updateStudy(Long studyId, StudyCreateUpdateRequest request, UserDetails userDetails) {
        studyPermissionChecker.validateIsInstructor(studyId, userDetails);

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
    }
}
