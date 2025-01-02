package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.domain.SurveyDto;
import aegis.server.domain.survey.repository.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public SurveyDto save(SurveyDto surveyDto) {

        Member member = memberRepository.findById(surveyDto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Survey survey = new Survey(
                member,
                surveyDto.getInterestFields(),
                surveyDto.getFeedBack(),
                surveyDto.getRegistrationReason());
        Survey save = surveyRepository.save(survey);

        return new SurveyDto(
                save.getMember().getId(),
                save.getInterestFields(),
                save.getRegistrationReason(),
                save.getFeedBack());

    }

    public SurveyDto findByMemberId(Long memberId) {
        Survey survey = surveyRepository.findByMemberId(memberId).orElseThrow(() -> new EntityNotFoundException("설문을 찾을 수 없습니다"));
        return new SurveyDto(
                survey.getMember().getId(),
                survey.getInterestFields(),
                survey.getRegistrationReason(),
                survey.getFeedBack());
    }


}

