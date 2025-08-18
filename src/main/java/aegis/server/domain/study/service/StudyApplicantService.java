package aegis.server.domain.study.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationDetail;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationStatus;
import aegis.server.domain.study.repository.StudyApplicationRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyApplicantService {

    private final StudyApplicationRepository studyApplicationRepository;

    public ApplicantStudyApplicationStatus getStudyApplicationStatus(Long studyId, UserDetails userDetails) {
        StudyApplication studyApplication = getStudyApplicationByStudyIdAndMemberId(studyId, userDetails.getMemberId());

        return ApplicantStudyApplicationStatus.from(studyApplication);
    }

    public ApplicantStudyApplicationDetail getStudyApplicationDetail(Long studyId, UserDetails userDetails) {
        StudyApplication studyApplication = getStudyApplicationByStudyIdAndMemberId(studyId, userDetails.getMemberId());

        return ApplicantStudyApplicationDetail.from(studyApplication);
    }

    @Transactional
    public ApplicantStudyApplicationDetail updateStudyApplicationReason(
            Long studyId, StudyEnrollRequest request, UserDetails userDetails) {
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
}
