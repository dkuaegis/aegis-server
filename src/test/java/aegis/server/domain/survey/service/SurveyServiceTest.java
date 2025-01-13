package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.repository.MemberRepository;
import aegis.server.domain.survey.domain.InterestField;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyRequest;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.security.dto.SessionUser;
import aegis.server.global.security.oidc.UserAuthInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
    void 새로운_설문_저장() {
        // given
        Member member = Member.createGuestMember("123456789012345678901", "test@dankook.ac.kr", "테스트");
        ReflectionTestUtils.setField(member, "id", 1L);

        UserAuthInfo userAuthInfo = UserAuthInfo.from(member);
        SessionUser sessionUser = SessionUser.from(userAuthInfo);

        SurveyRequest surveyRequest = SurveyRequest.builder()
                .interestFields(Set.of(InterestField.GAME_CLIENT))
                .interestEtc(new HashMap<>())
                .registrationReason("동아리에서 어떤 활동을 하고 싶으신가요?")
                .feedBack("예시로 작년과는 이런 점이 달라졌으면 좋겠어요!")
                .build();

        when(memberRepository.findById(sessionUser.getId()))
                .thenReturn(Optional.of(member));
        when(surveyRepository.findByMemberId(member.getId()))
                .thenReturn(Optional.empty());

        // when
        surveyService.save(surveyRequest, sessionUser);

        // then
        verify(surveyRepository).save(any(Survey.class));
    }

    @Test
    void 기존_설문_업데이트() {
        // given
        Member member = Member.createGuestMember("123456789012345678901", "test@dankook.ac.kr", "테스트");
        ReflectionTestUtils.setField(member, "id", 1L);

        UserAuthInfo userAuthInfo = UserAuthInfo.from(member);
        SessionUser sessionUser = SessionUser.from(userAuthInfo);

        Survey existingSurvey = Survey.createSurvey(member, SurveyRequest.builder()
                .interestFields(Set.of(InterestField.GAME_CLIENT))
                .interestEtc(new HashMap<>())
                .registrationReason("이전 이유")
                .feedBack("이전 피드백")
                .build());

        SurveyRequest updateDto = SurveyRequest.builder()
                .interestFields(Set.of(InterestField.WEB_BACKEND))
                .interestEtc(new HashMap<>())
                .registrationReason("새로운 이유")
                .feedBack("새로운 피드백")
                .build();

        when(memberRepository.findById(sessionUser.getId()))
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
    void 회원_ID로_설문_조회() {
        // given
        Member member = Member.createGuestMember("123456789012345678901", "test@dankook.ac.kr", "테스트");
        ReflectionTestUtils.setField(member, "id", 1L);

        Survey survey = Survey.createSurvey(member, SurveyRequest.builder()
                .interestFields(Set.of(InterestField.GAME_CLIENT))
                .interestEtc(new HashMap<>())
                .registrationReason("테스트 이유")
                .feedBack("테스트 피드백")
                .build());

        when(surveyRepository.findByMemberId(member.getId()))
                .thenReturn(Optional.of(survey));

        // when
        SurveyRequest foundSurvey = surveyService.findByMemberId(member.getId());

        // then
        assertNotNull(foundSurvey);
        assertEquals(Set.of(InterestField.GAME_CLIENT), foundSurvey.getInterestFields());
        assertEquals("테스트 이유", foundSurvey.getRegistrationReason());
        assertEquals("테스트 피드백", foundSurvey.getFeedBack());
    }

    @Test
    void 모든_설문_조회() {
        // given
        Member member1 = Member.createGuestMember("123456789012345678901", "test1@dankook.ac.kr", "테스트1");
        Member member2 = Member.createGuestMember("123456789012345678902", "test2@dankook.ac.kr", "테스트2");
        ReflectionTestUtils.setField(member1, "id", 1L);
        ReflectionTestUtils.setField(member2, "id", 2L);

        List<Survey> surveys = Arrays.asList(
                Survey.createSurvey(member1, SurveyRequest.builder()
                        .interestFields(Set.of(InterestField.GAME_CLIENT))
                        .registrationReason("이유1")
                        .feedBack("피드백1")
                        .build()),
                Survey.createSurvey(member2, SurveyRequest.builder()
                        .interestFields(Set.of(InterestField.WEB_BACKEND))
                        .registrationReason("이유2")
                        .feedBack("피드백2")
                        .build())
        );

        when(surveyRepository.findAll()).thenReturn(surveys);

        // when
        List<SurveyRequest> allSurveys = surveyService.getAllSurveys();

        // then
        assertEquals(2, allSurveys.size());
        assertEquals("이유1", allSurveys.get(0).getRegistrationReason());
        assertEquals("이유2", allSurveys.get(1).getRegistrationReason());
    }
}
