package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.survey.domain.AcquisitionType;
import aegis.server.domain.survey.domain.Interest;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import aegis.server.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
class SurveyServiceTest extends IntegrationTest {

    @Autowired
    SurveyService surveyService;

    @Autowired
    SurveyRepository surveyRepository;

    private final SurveyCommon validSurveyRequest = new SurveyCommon(
            Set.of(Interest.WEB_BACKEND, Interest.DEVOPS, Interest.GAME_ETC),
            Map.of(Interest.GAME_ETC, "게임 기획"),
            AcquisitionType.EVERYTIME,
            "가입 이유",
            "운영진에게 하고 싶은 말"
    );

    @Nested
    class 설문조사_저장_및_수정 {

        @Test
        void 새로운_설문조사_저장에_성공한다() {
            // given
            Member member = createMember();
            Student student = createStudent(member);
            UserDetails userDetails = createUserDetails(member);

            // when
            surveyService.createOrUpdateSurvey(userDetails, validSurveyRequest);

            //then
            Survey survey = surveyRepository.findByStudent(student).get();

            assertEquals(validSurveyRequest.interests(), survey.getInterests());
            assertEquals(validSurveyRequest.interestsEtc(), survey.getInterestsEtc());
            assertEquals(validSurveyRequest.joinReason(), survey.getJoinReason());
            assertEquals(validSurveyRequest.feedback(), survey.getFeedback());
        }

        @Test
        void 설문조사_수정에_성공한다() {
            // given
            Member member = createMember();
            Student student = createStudent(member);
            UserDetails userDetails = createUserDetails(member);
            surveyService.createOrUpdateSurvey(userDetails, validSurveyRequest);

            SurveyCommon updatedSurveyRequest = new SurveyCommon(
                    Set.of(Interest.AI, Interest.ETC),
                    Map.of(Interest.ETC, "임베디드"),
                    AcquisitionType.KAKAOTALK,
                    "업데이트된 사유",
                    "업데이트된 피드백"
            );

            // when
            surveyService.createOrUpdateSurvey(userDetails, updatedSurveyRequest);

            // then
            Survey survey = surveyRepository.findByStudent(student).get();

            assertEquals(updatedSurveyRequest.interests(), survey.getInterests());
            assertEquals(updatedSurveyRequest.interestsEtc(), survey.getInterestsEtc());
            assertEquals(updatedSurveyRequest.joinReason(), survey.getJoinReason());
            assertEquals(updatedSurveyRequest.feedback(), survey.getFeedback());
        }

        @Test
        void 올바르지_않은_관심사_입력시_실패한다_1() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            createStudent(member);

            SurveyCommon invalidSurveyRequest = new SurveyCommon(
                    Set.of(Interest.WEB_BACKEND),
                    Map.of(Interest.AI, "인공지능"),
                    AcquisitionType.KAKAOTALK,
                    "가입 이유",
                    "운영진에게 하고 싶은 말"
            );

            // when-then
            CustomException exception = assertThrows(CustomException.class, () ->
                    surveyService.createOrUpdateSurvey(userDetails, invalidSurveyRequest)
            );
            assertEquals(ErrorCode.INVALID_INTEREST, exception.getErrorCode());
        }

        @Test
        void 올바르지_않은_관심사_입력시_실패한다_2() {
            // given
            Member member = createMember();
            UserDetails userDetails = createUserDetails(member);
            createStudent(member);

            SurveyCommon invalidSurveyRequest = new SurveyCommon(
                    Set.of(Interest.WEB_BACKEND),
                    Map.of(Interest.ETC, "임베디드"),
                    AcquisitionType.KAKAOTALK,
                    "가입 이유",
                    "운영진에게 하고 싶은 말"
            );

            // when-then
            CustomException exception = assertThrows(CustomException.class, () ->
                    surveyService.createOrUpdateSurvey(userDetails, invalidSurveyRequest)
            );
            assertEquals(ErrorCode.ETC_INTEREST_NOT_FOUND, exception.getErrorCode());
        }
    }
}
