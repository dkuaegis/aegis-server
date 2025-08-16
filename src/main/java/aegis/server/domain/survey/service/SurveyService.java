package aegis.server.domain.survey.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;

    public SurveyCommon getSurvey(UserDetails userDetails) {
        Survey survey = surveyRepository
                .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND));

        return SurveyCommon.from(survey);
    }

    @Transactional
    public void createOrUpdateSurvey(UserDetails userDetails, SurveyCommon surveyCommon) {
        Member member = findMemberById(userDetails.getMemberId());
        surveyRepository
                .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                .ifPresentOrElse(
                        survey -> survey.update(surveyCommon.acquisitionType(), surveyCommon.joinReason()),
                        () -> surveyRepository.save(
                                Survey.create(member, surveyCommon.acquisitionType(), surveyCommon.joinReason())));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
