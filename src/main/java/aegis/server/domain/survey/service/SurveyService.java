package aegis.server.domain.survey.service;

import aegis.server.domain.member.domain.Student;
import aegis.server.domain.member.repository.StudentRepository;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final StudentRepository studentRepository;

    public SurveyCommon getSurvey(UserDetails userDetails) {
        Student student = findStudentByMemberId(userDetails.getMemberId());
        Survey survey = surveyRepository.findByStudent(student)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND));

        return SurveyCommon.from(survey);
    }

    @Transactional
    public void createOrUpdateSurvey(UserDetails userDetails, SurveyCommon surveyCommon) {
        Student student = findStudentByMemberId(userDetails.getMemberId());
        surveyRepository.findByStudent(student)
                .ifPresentOrElse(
                        survey -> survey.update(
                                surveyCommon.interests(),
                                surveyCommon.interestsEtc(),
                                surveyCommon.acquisitionType(),
                                surveyCommon.joinReason(),
                                surveyCommon.feedback()
                        ),
                        () -> surveyRepository.save(
                                Survey.create(
                                        student,
                                        surveyCommon.interests(),
                                        surveyCommon.interestsEtc(),
                                        surveyCommon.acquisitionType(),
                                        surveyCommon.joinReason(),
                                        surveyCommon.feedback()
                                )
                        )
                );
    }

    private Student findStudentByMemberId(Long memberId) {
        return studentRepository.findByMemberIdInCurrentYearSemester(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
