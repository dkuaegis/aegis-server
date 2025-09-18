package aegis.server.domain.googlesheets.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.googlesheets.dto.ImportData;
import aegis.server.domain.googlesheets.dto.PointShopDrawData;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.dto.internal.PaymentInfo;
import aegis.server.domain.pointshop.dto.internal.PointShopDrawInfo;
import aegis.server.domain.survey.domain.Survey;
import aegis.server.domain.survey.repository.SurveyRepository;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Profile("!test")
@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private final Sheets sheets;
    private final SurveyRepository surveyRepository;

    @Value("${google.spreadsheets.registration.id}")
    private String registrationSpreadsheetId;

    private static final String REGISTRATION_SHEET_RANGE = "database!A2:O";

    @Value("${google.spreadsheets.pointshop.id}")
    private String pointShopDrawSpreadsheetId;

    private static final String POINT_SHOP_DRAW_SHEET_RANGE = "record!A2:F";

    public void addMemberRegistration(Member member, PaymentInfo paymentInfo) throws IOException {
        Survey survey = surveyRepository
                .findByMemberIdInCurrentYearSemester(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND));

        ImportData importData = new ImportData(
                paymentInfo.updatedAt(),
                member.getName(),
                member.getStudentId(),
                member.getDepartment(),
                member.getGrade(),
                member.getPhoneNumber(),
                member.getDiscordId(),
                member.getEmail(),
                member.getBirthdate(),
                member.getGender(),
                survey.getAcquisitionType(),
                survey.getJoinReason(),
                paymentInfo.finalPrice());

        ValueRange body = new ValueRange().setValues(List.of(importData.toRowData()));

        sheets.spreadsheets()
                .values()
                .append(registrationSpreadsheetId, REGISTRATION_SHEET_RANGE, body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }

    public void addPointShopDraw(Member member, PointShopDrawInfo drawInfo) throws IOException {
        PointShopDrawData row = PointShopDrawData.from(drawInfo, member);

        ValueRange body = new ValueRange().setValues(List.of(row.toRowData()));

        sheets.spreadsheets()
                .values()
                .append(pointShopDrawSpreadsheetId, POINT_SHOP_DRAW_SHEET_RANGE, body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute();
    }
}
