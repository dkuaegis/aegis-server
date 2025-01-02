package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.survey.domain.InterestField;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.domain.SurveyDto;
import aegis.server.domain.survey.repository.SurveyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SurveyService surveyService;

    @Test
    void saveSurveySuccess() throws Exception {
        //given
        Long memberId = 1L;
        Member member = new Member();
        HashSet<InterestField> interestFields = new HashSet<>(Arrays.asList(InterestField.DEVOPS, InterestField.GAME_SERVER, InterestField.NOTSURE));
        String registrationReason = "동이리 가입 이유";
        String feedBack = "하고 싶은 말";

        SurveyDto surveyDto = new SurveyDto(memberId, interestFields, registrationReason, feedBack);
        Survey survey = new Survey(member, interestFields, registrationReason, feedBack);

        when(memberRepository.findById(memberId)).thenReturn(Optional.ofNullable(member));
        when(surveyRepository.save(any(Survey.class))).thenReturn(survey);

        //when
        SurveyDto saveDto = surveyService.save(surveyDto);

        //then
        assertNotNull(saveDto);
        assertEquals(registrationReason, saveDto.getRegistrationReason());
        assertEquals(interestFields, saveDto.getInterestFields());
        assertEquals(feedBack, saveDto.getFeedBack());

        verify(memberRepository).findById(memberId);
        verify(surveyRepository).save(any(Survey.class));
    }

    @Test
    void saveSurveyEx() throws Exception {
        //given
        Long memberId = 999L;
        SurveyDto surveyDto = new SurveyDto(memberId, new HashSet<>(), "reason", "feedBack");

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        //when & then
        assertThrows(EntityNotFoundException.class,
                () -> surveyService.save(surveyDto));
    }

    @Test
    void findByMemberIdSuccess() throws Exception {
        //given
        Long memberId = 1L;
        Member member = new Member();
        Set<InterestField> interests = new HashSet<>(
                Arrays.asList(InterestField.GAME_CLIENT, InterestField.AI));
        Survey survey = new Survey(member, interests, "reason", "feedBack");
        when(surveyRepository.findByMemberId(memberId)).thenReturn(Optional.of(survey));

        //when
        SurveyDto findDto = surveyService.findByMemberId(memberId);

        //then
        assertNotNull(findDto);
        assertEquals(interests, findDto.getInterestFields());
        verify(surveyRepository).findByMemberId(memberId);
    }
}