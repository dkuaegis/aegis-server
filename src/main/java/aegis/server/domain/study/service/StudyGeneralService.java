package aegis.server.domain.study.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.study.domain.*;
import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.GeneralStudyRolesIdsResponse;
import aegis.server.domain.study.dto.response.GeneralStudySummary;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyMemberRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyGeneralService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    public List<GeneralStudySummary> findAllStudies() {
        return studyRepository.findStudySummariesByCurrentYearSemester();
    }

    public GeneralStudyDetail getStudyDetail(Long studyId) {
        return studyRepository
                .findStudyDetailById(studyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));
    }

    public GeneralStudyRolesIdsResponse getMyStudyRoles(UserDetails userDetails) {
        Member member = getMemberById(userDetails.getMemberId());

        List<Long> instructorStudyIds = studyMemberRepository.findStudyIdsByMemberIdAndRoleAndYearSemester(
                member.getId(), StudyRole.INSTRUCTOR, CURRENT_YEAR_SEMESTER);
        List<Long> participantStudyIds = studyMemberRepository.findStudyIdsByMemberIdAndRoleAndYearSemester(
                member.getId(), StudyRole.PARTICIPANT, CURRENT_YEAR_SEMESTER);
        List<Long> appliedStudyIds = studyApplicationRepository.findAppliedStudyIdsByMemberIdAndStatusAndYearSemester(
                member.getId(), StudyApplicationStatus.PENDING, CURRENT_YEAR_SEMESTER);

        return GeneralStudyRolesIdsResponse.from(instructorStudyIds, participantStudyIds, appliedStudyIds);
    }

    @Transactional
    public GeneralStudyDetail createStudy(StudyCreateUpdateRequest request, UserDetails userDetails) {
        Member member = getMemberById(userDetails.getMemberId());

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

        return getStudyDetail(study.getId());
    }

    @Transactional
    public void enrollInStudy(Long studyId, StudyEnrollRequest request, UserDetails userDetails) {
        Member member = getMemberById(userDetails.getMemberId());
        Study study = studyRepository
                .findByIdWithLock(studyId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

        if (study.getRecruitmentMethod() == StudyRecruitmentMethod.FCFS) {
            processFCFS(study, member);
            log.info(
                    "[Study][Enroll][FCFS] 가입 완료: studyId={}, memberId={}, memberName={}, currentParticipants={}, maxParticipants={}",
                    study.getId(),
                    member.getId(),
                    member.getName(),
                    study.getCurrentParticipants(),
                    study.getMaxParticipants());
        } else if (study.getRecruitmentMethod() == StudyRecruitmentMethod.APPLICATION) {
            processApplication(study, member, request.applicationReason());
            studyApplicationRepository
                    .findByStudyAndMember(study, member)
                    .ifPresent(sa -> log.info(
                            "[Study][Enroll][APPLICATION] 신청 접수: studyId={}, applicationId={}, applicantId={}, applicantName={}",
                            study.getId(),
                            sa.getId(),
                            member.getId(),
                            member.getName()));
        } else {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void processFCFS(Study study, Member member) {
        validateNotAlreadyEnrolled(study, member);
        study.increaseCurrentParticipant();

        StudyMember studyMember = StudyMember.create(study, member, StudyRole.PARTICIPANT);
        studyMemberRepository.save(studyMember);
    }

    private void processApplication(Study study, Member member, String applicationReason) {
        validateNotAlreadyEnrolled(study, member);
        validateNotAlreadyApplied(study, member);

        StudyApplication studyApplication = StudyApplication.create(study, member, applicationReason);
        studyApplicationRepository.save(studyApplication);
    }

    private void validateNotAlreadyEnrolled(Study study, Member member) {
        if (studyMemberRepository.existsByStudyAndMember(study, member)) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_ALREADY_EXISTS);
        }
    }

    private void validateNotAlreadyApplied(Study study, Member member) {
        if (studyApplicationRepository.existsByStudyAndMember(study, member)) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_ALREADY_EXISTS);
        }
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
