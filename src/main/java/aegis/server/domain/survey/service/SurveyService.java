package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyRequest;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.security.dto.SessionUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void save(SurveyRequest surveyRequest, SessionUser sessionUser) {
        Member findMember = memberRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalStateException("구글 인증된 사용자가 존재하지 않습니다."));

        surveyRepository.findByMemberId(findMember.getId())
                .ifPresentOrElse(
                        survey -> survey.update(surveyRequest),
                        () -> {
                            Survey newSurvey = Survey.createSurvey(findMember, surveyRequest);
                            surveyRepository.save(newSurvey);
                        }
                );
    }

    public SurveyRequest findByMemberId(Long memberId) {
        Survey survey = surveyRepository.findByMemberId(memberId).orElseThrow(() -> new EntityNotFoundException("설문을 찾을 수 없습니다"));
        return SurveyRequest.builder()
                .interestFields(survey.getInterestFields())
                .interestEtc(survey.getInterestEtc())
                .registrationReason(survey.getRegistrationReason())
                .feedBack(survey.getFeedBack())
                .build();
    }


    public List<SurveyRequest> getAllSurveys() {
        return surveyRepository.findAll().stream().map(s ->
                SurveyRequest.builder()
                        .interestFields(s.getInterestFields())
                        .interestEtc(s.getInterestEtc())
                        .registrationReason(s.getRegistrationReason())
                        .feedBack(s.getFeedBack())
                        .build()).toList();
    }

}

