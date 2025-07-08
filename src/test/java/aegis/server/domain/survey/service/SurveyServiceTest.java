package aegis.server.domain.survey.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class SurveyServiceTest extends IntegrationTest {

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyRepository surveyRepository;

    private final SurveyCommon validSurveyRequest = new SurveyCommon(AcquisitionType.EVERYTIME, "가입 이유");

    @Nested
    class 설문조사_저장_및_수정 {

        @Test
        void 새로운_설문조사_저장에_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            surveyService.createOrUpdateSurvey(userDetails, validSurveyRequest);

            // then
            Survey survey = surveyRepository.findByMember(member).get();

            assertEquals(validSurveyRequest.acquisitionType(), survey.getAcquisitionType());
            assertEquals(validSurveyRequest.joinReason(), survey.getJoinReason());
        }

        @Test
        void 설문조사_수정에_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            surveyService.createOrUpdateSurvey(userDetails, validSurveyRequest);

            SurveyCommon updatedSurveyRequest = new SurveyCommon(AcquisitionType.KAKAOTALK, "업데이트된 사유");

            // when
            surveyService.createOrUpdateSurvey(userDetails, updatedSurveyRequest);

            // then
            Survey survey = surveyRepository.findByMember(member).get();

            assertEquals(updatedSurveyRequest.acquisitionType(), survey.getAcquisitionType());
            assertEquals(updatedSurveyRequest.joinReason(), survey.getJoinReason());
        }
    }
}
