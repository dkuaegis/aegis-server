package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.survey.domain.InterestField;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.domain.SurveyDto;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.security.dto.SessionUser;
import aegis.server.global.security.oidc.UserAuthInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.ArgumentMatchers.any;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {
    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SurveyService surveyService;

    @Test
    @DisplayName("새로운 설문 저장")
    void saveNewSurvey() {
        // given
        Member member = Member.createGuestMember("test@dankook.ac.kr", "테스트");
        ReflectionTestUtils.setField(member, "id", 1L);

        UserAuthInfo userAuthInfo = UserAuthInfo.from(member);
        SessionUser sessionUser = SessionUser.from(userAuthInfo);

        SurveyDto surveyDto = SurveyDto.builder()
                .interestFields(Set.of(InterestField.GAME_CLIENT))
                .interestEtc(new HashMap<>())
                .registrationReason("동아리에서 어떤 활동을 하고 싶으신가요?")
                .feedBack("예시로 작년과는 이런 점이 달라졌으면 좋겠어요!")
                .build();

        when(memberRepository.findByEmail(sessionUser.getEmail()))
                .thenReturn(Optional.of(member));
        when(surveyRepository.findByMemberId(member.getId()))
                .thenReturn(Optional.empty());

        // when
        surveyService.save(surveyDto, sessionUser);

        // then
        verify(surveyRepository).save(any(Survey.class));
    }

    @Test
    @DisplayName("기존 설문 업데이트")
    void updateExistingSurvey() {
        // given
        Member member = Member.createGuestMember("test@dankook.ac.kr", "테스트");
        ReflectionTestUtils.setField(member, "id", 1L);

        UserAuthInfo userAuthInfo = UserAuthInfo.from(member);
        SessionUser sessionUser = SessionUser.from(userAuthInfo);

        Survey existingSurvey = Survey.builder()
                .member(member)
                .interestFields(Set.of(InterestField.GAME_CLIENT))
                .interestEtc(new HashMap<>())
                .registrationReason("이전 이유")
                .feedBack("이전 피드백")
                .build();

        SurveyDto updateDto = SurveyDto.builder()
                .interestFields(Set.of(InterestField.WEB_BACKEND))
                .interestEtc(new HashMap<>())
                .registrationReason("새로운 이유")
                .feedBack("새로운 피드백")
                .build();

        when(memberRepository.findByEmail(sessionUser.getEmail()))
                .thenReturn(Optional.of(member));
        when(surveyRepository.findByMemberId(member.getId()))
                .thenReturn(Optional.of(existingSurvey));

        // when
        surveyService.save(updateDto, sessionUser);

        // then
        assertEquals(Set.of(InterestField.WEB_BACKEND), existingSurvey.getInterestFields());
        assertEquals("새로운 이유", existingSurvey.getRegistrationReason());
        assertEquals("새로운 피드백", existingSurvey.getFeedBack());
    }

    @Test
    @DisplayName("회원 ID로 설문 조회")
    void findByMemberId() {
        // given
        Member member = Member.createGuestMember("test@dankook.ac.kr", "테스트");
        ReflectionTestUtils.setField(member, "id", 1L);

        Survey survey = Survey.builder()
                .member(member)
                .interestFields(Set.of(InterestField.GAME_CLIENT))
                .interestEtc(new HashMap<>())
                .registrationReason("테스트 이유")
                .feedBack("테스트 피드백")
                .build();

        when(surveyRepository.findByMemberId(member.getId()))
                .thenReturn(Optional.of(survey));

        // when
        SurveyDto foundSurvey = surveyService.findByMemberId(member.getId());

        // then
        assertNotNull(foundSurvey);
        assertEquals(Set.of(InterestField.GAME_CLIENT), foundSurvey.getInterestFields());
        assertEquals("테스트 이유", foundSurvey.getRegistrationReason());
        assertEquals("테스트 피드백", foundSurvey.getFeedBack());
    }

    @Test
    @DisplayName("모든 설문 조회")
    void getAllSurveys() {
        // given
        Member member1 = Member.createGuestMember("test1@dankook.ac.kr", "테스트1");
        Member member2 = Member.createGuestMember("test2@dankook.ac.kr", "테스트2");
        ReflectionTestUtils.setField(member1, "id", 1L);
        ReflectionTestUtils.setField(member2, "id", 2L);

        List<Survey> surveys = Arrays.asList(
                Survey.builder()
                        .member(member1)
                        .interestFields(Set.of(InterestField.GAME_CLIENT))
                        .registrationReason("이유1")
                        .feedBack("피드백1")
                        .build(),
                Survey.builder()
                        .member(member2)
                        .interestFields(Set.of(InterestField.WEB_BACKEND))
                        .registrationReason("이유2")
                        .feedBack("피드백2")
                        .build()
        );

        when(surveyRepository.findAll()).thenReturn(surveys);

        // when
        List<SurveyDto> allSurveys = surveyService.getAllSurveys();

        // then
        assertEquals(2, allSurveys.size());
        assertEquals("이유1", allSurveys.get(0).getRegistrationReason());
        assertEquals("이유2", allSurveys.get(1).getRegistrationReason());
    }
}
