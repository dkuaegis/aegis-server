package aegis.server.domain.survey.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.member.domain.*;
import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurveyServiceTest extends IntegrationTest {

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyRepository surveyRepository;

    @Nested
    class 설문조사_조회 {

        @Test
        void 설문조사_조회에_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            SurveyCommon surveyRequest = new SurveyCommon(AcquisitionType.EVERYTIME, "가입 이유");
            surveyService.createOrUpdateSurvey(userDetails, surveyRequest);

            // when
            SurveyCommon result = surveyService.getSurvey(userDetails);

            // then
            assertEquals(surveyRequest.acquisitionType(), result.acquisitionType());
            assertEquals(surveyRequest.joinReason(), result.joinReason());
        }

        @Test
        void 설문조사가_존재하지_않을_때_예외를_발생시킨다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when & then
            CustomException exception = assertThrows(CustomException.class, () -> surveyService.getSurvey(userDetails));
            assertEquals(ErrorCode.SURVEY_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class 설문조사_저장_및_수정 {

        @Test
        void 새로운_설문조사_저장에_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            // when
            SurveyCommon validSurveyRequest = new SurveyCommon(AcquisitionType.EVERYTIME, "가입 이유");
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

            SurveyCommon validSurveyRequest = new SurveyCommon(AcquisitionType.EVERYTIME, "가입 이유");
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
