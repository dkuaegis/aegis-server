package aegis.server.domain.study.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyRecruitmentMethod;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationDetail;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationStatus;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.domain.study.repository.StudyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyApplicantService {

    private final StudyApplicationRepository studyApplicationRepository;
    private final StudyRepository studyRepository;

    public ApplicantStudyApplicationStatus getStudyApplicationStatus(Long studyId, UserDetails userDetails) {
        // 스터디 존재 확인 및 FCFS 미지원 검증
        validateApplicationSupported(studyId);

        // 지원서 스터디: 미제출이면 NONE 반환
        return studyApplicationRepository
                .findByStudyIdAndMemberId(studyId, userDetails.getMemberId())
                .map(ApplicantStudyApplicationStatus::from)
                .orElseGet(ApplicantStudyApplicationStatus::none);
    }

    public ApplicantStudyApplicationDetail getStudyApplicationDetail(Long studyId, UserDetails userDetails) {
        // 스터디 존재 확인 및 FCFS 미지원 검증
        validateApplicationSupported(studyId);

        StudyApplication studyApplication = getStudyApplicationByStudyIdAndMemberId(studyId, userDetails.getMemberId());
        return ApplicantStudyApplicationDetail.from(studyApplication);
    }

    @Transactional
    public ApplicantStudyApplicationDetail updateStudyApplicationReason(
            Long studyId, StudyEnrollRequest request, UserDetails userDetails) {
        // 스터디 존재 확인 및 FCFS 미지원 검증
        validateApplicationSupported(studyId);

        StudyApplication studyApplication = getStudyApplicationByStudyIdAndMemberId(studyId, userDetails.getMemberId());

        studyApplication.validateApplicationUpdatable();
        studyApplication.updateApplicationReason(request.applicationReason());

        return ApplicantStudyApplicationDetail.from(studyApplication);
    }

    private StudyApplication getStudyApplicationByStudyIdAndMemberId(Long studyId, Long memberId) {
        return studyApplicationRepository
                .findByStudyIdAndMemberId(studyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_APPLICATION_NOT_FOUND));
    }

    private void validateApplicationSupported(Long studyId) {
        Study study =
                studyRepository.findById(studyId).orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));
        if (study.getRecruitmentMethod() == StudyRecruitmentMethod.FCFS) {
            throw new CustomException(ErrorCode.STUDY_APPLICATION_STATUS_NOT_SUPPORTED);
        }
    }
}
