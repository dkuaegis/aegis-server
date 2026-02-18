package aegis.server.domain.survey.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.*;
import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;
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
            Survey survey = surveyRepository
                    .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                    .get();

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

            SurveyCommon updatedSurveyRequest = new SurveyCommon(AcquisitionType.FRIEND, "업데이트된 사유");

            // when
            surveyService.createOrUpdateSurvey(userDetails, updatedSurveyRequest);

            // then
            Survey survey = surveyRepository
                    .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                    .get();

            assertEquals(updatedSurveyRequest.acquisitionType(), survey.getAcquisitionType());
            assertEquals(updatedSurveyRequest.joinReason(), survey.getJoinReason());
        }

        @Test
        void 이전_학기_설문조사_존재_후_새_학기_설문조사_생성에_성공한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            SurveyCommon firstSurveyRequest = new SurveyCommon(AcquisitionType.EVERYTIME, "첫 번째 학기 가입 이유");
            surveyService.createOrUpdateSurvey(userDetails, firstSurveyRequest);

            // 기존 설문조사를 이전 학기로 변경
            Survey oldSurvey = surveyRepository
                    .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                    .get();
            ReflectionTestUtils.setField(oldSurvey, "yearSemester", YearSemester.YEAR_SEMESTER_2025_1);
            surveyRepository.save(oldSurvey);

            SurveyCommon newSurveyRequest = new SurveyCommon(AcquisitionType.FRIEND, "새 학기 가입 이유");

            // when
            surveyService.createOrUpdateSurvey(userDetails, newSurveyRequest);

            // then
            Survey newSurvey = surveyRepository
                    .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                    .get();

            assertEquals(newSurveyRequest.acquisitionType(), newSurvey.getAcquisitionType());
            assertEquals(newSurveyRequest.joinReason(), newSurvey.getJoinReason());
            assertEquals(CURRENT_YEAR_SEMESTER, newSurvey.getYearSemester());
        }

        @Test
        void 새_학기_시작_후_설문조사_조회_시_예외가_발생한다() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);

            SurveyCommon surveyRequest = new SurveyCommon(AcquisitionType.EVERYTIME, "가입 이유");
            surveyService.createOrUpdateSurvey(userDetails, surveyRequest);

            // 기존 설문조사를 이전 학기로 변경
            Survey oldSurvey = surveyRepository
                    .findByMemberIdInCurrentYearSemester(userDetails.getMemberId())
                    .get();
            ReflectionTestUtils.setField(oldSurvey, "yearSemester", YearSemester.YEAR_SEMESTER_2025_1);
            surveyRepository.save(oldSurvey);

            // when & then
            CustomException exception = assertThrows(CustomException.class, () -> surveyService.getSurvey(userDetails));
            assertEquals(ErrorCode.SURVEY_NOT_FOUND, exception.getErrorCode());
        }
    }
}
