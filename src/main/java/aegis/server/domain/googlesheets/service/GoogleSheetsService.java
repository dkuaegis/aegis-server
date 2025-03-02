package aegis.server.domain.googlesheets.service;

import aegis.server.domain.googlesheets.dto.ImportData;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private final Sheets sheets;
    private final SurveyRepository surveyRepository;

    @Value("${google.spreadsheets.id}")
    private String spreadsheetId;

    private static final String REGISTRATION_SHEET_RANGE = "database!A2:R";

    public void addMemberRegistration(Member member, Student student, PaymentInfo paymentInfo) throws IOException {
        Survey survey = surveyRepository.findByStudent(student)
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND));

        ImportData importData = new ImportData(
                paymentInfo.updatedAt(),
                member.getName(),
                student.getStudentId(),
                student.getDepartment(),
                student.getGrade(),
                student.getSemester(),
                student.getAcademicStatus(),
                member.getPhoneNumber(),
                member.getDiscordId(),
                member.getEmail(),
                member.getBirthdate(),
                member.getGender(),
                student.getFresh(),
                survey.getInterests(),
                survey.getAcquisitionType(),
                survey.getJoinReason(),
                survey.getFeedback(),
                paymentInfo.finalPrice()
        );

        ValueRange body = new ValueRange()
                .setValues(List.of(importData.toRowData()));

        sheets.spreadsheets().values()
                .append(spreadsheetId, REGISTRATION_SHEET_RANGE, body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }
}
