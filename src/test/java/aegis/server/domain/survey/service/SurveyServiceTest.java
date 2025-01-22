package aegis.server.domain.survey.service;

import aegis.server.common.IntegrationTest;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.survey.domain.InterestField;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyRequest;
import aegis.server.global.security.dto.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static aegis.server.global.constant.Constant.CURRENT_SEMESTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SurveyServiceTest extends IntegrationTest {

    @Autowired
    private SurveyService surveyService;


    @Test
    void 새로운_설문_저장() {
        // given
        Member member = createMember();
        SessionUser sessionUser = createSessionUser(member);
        SurveyRequest surveyRequest = createSurveyRequest();

        //when
        surveyService.save(surveyRequest, sessionUser);

        //then
        Survey survey = surveyRepository.findByMemberIdAndCurrentSemester(member.getId(),
                CURRENT_SEMESTER).orElseThrow();
        assertEquals(member.getId(), survey.getMember().getId());
        assertEquals(surveyRequest.getInterestFields(), survey.getInterestFields());
        assertEquals(surveyRequest.getInterestEtc(), survey.getInterestEtc());
        assertEquals(surveyRequest.getRegistrationReason(), survey.getRegistrationReason());
        assertEquals(surveyRequest.getFeedBack(), survey.getFeedBack());
    }

    @Test
    void 기존_설문_업데이트() {
        // given
        Member member = createMember();
        SessionUser sessionUser = createSessionUser(member);
        SurveyRequest surveyRequest = createSurveyRequest();
        surveyService.save(surveyRequest, sessionUser);

        //when
        SurveyRequest updatedSurveyRequest = new SurveyRequest(
                Set.of(InterestField.AI),
                Map.of(InterestField.GAME_ETC, "게임 기타"),
                "업데이트된 사유",
                "업데이트된 피드백"
        );
        surveyService.save(updatedSurveyRequest, sessionUser);

        //then
        Survey survey = surveyRepository.findByMemberIdAndCurrentSemester(member.getId(),
                CURRENT_SEMESTER).orElseThrow();
        assertEquals(member.getId(), survey.getMember().getId());
        assertEquals(updatedSurveyRequest.getInterestFields(), survey.getInterestFields());
        assertEquals(updatedSurveyRequest.getInterestEtc(), survey.getInterestEtc());
        assertEquals(updatedSurveyRequest.getRegistrationReason(), survey.getRegistrationReason());
        assertEquals(updatedSurveyRequest.getFeedBack(), survey.getFeedBack());

    }

    @Test
    void 회원_ID로_설문_조회() {
        // given
        Member member = createMember();
        SessionUser sessionUser = createSessionUser(member);
        SurveyRequest surveyRequest = createSurveyRequest();
        surveyService.save(surveyRequest, sessionUser);

        //when
        SurveyRequest findSurveyRequest = surveyService.findByMemberId(member.getId());

        //then
        assertEquals(surveyRequest.getInterestFields(), findSurveyRequest.getInterestFields());
        assertEquals(surveyRequest.getInterestEtc(), findSurveyRequest.getInterestEtc());
        assertEquals(surveyRequest.getRegistrationReason(), findSurveyRequest.getRegistrationReason());
        assertEquals(surveyRequest.getFeedBack(), findSurveyRequest.getFeedBack());
    }

    @Test
    void 모든_설문_조회() {
        // given
        Member member = createMember();
        SessionUser sessionUser = createSessionUser(member);
        SurveyRequest surveyRequest = createSurveyRequest();
        surveyService.save(surveyRequest, sessionUser);

        //when
        List<SurveyRequest> allSurveys = surveyService.getAllSurveys();

        //then
        assertEquals(1, allSurveys.size());
    }
}
