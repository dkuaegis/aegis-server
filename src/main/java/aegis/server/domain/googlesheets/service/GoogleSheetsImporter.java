// package aegis.server.domain.googlesheets.service;
//
// import aegis.server.domain.googlesheets.dto.ImportData;
// import aegis.server.domain.member.domain.Member;
// import aegis.server.domain.member.domain.Student;
// import aegis.server.domain.payment.domain.Payment;
// import aegis.server.domain.payment.domain.PaymentStatus;
// import aegis.server.domain.payment.repository.PaymentRepository;
// import aegis.server.domain.survey.domain.Survey;
// import aegis.server.domain.survey.repository.SurveyRepository;
// import aegis.server.global.exception.CustomException;
// import aegis.server.global.exception.ErrorCode;
// import com.google.api.services.sheets.v4.Sheets;
// import com.google.api.services.sheets.v4.model.ValueRange;
// import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.List;
// import java.util.stream.Collectors;
//
// import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;
//
// @RestController
// @RequestMapping("/internal/importer")
// @RequiredArgsConstructor
// public class GoogleSheetsImporter {
//
//    private final SurveyRepository surveyRepository;
//    private final PaymentRepository paymentRepository;
//
//    private final Sheets sheets;
//
//    @Value("${google.spreadsheets.id}")
//    private String spreadsheetId;
//
//    private static final String REGISTRATION_SHEET_RANGE = "database!A2:R";
//
//    @Transactional
//    @GetMapping
//    public void importEntireData() throws IOException {
//        List<Payment> payments = paymentRepository.findAllByStatusAndYearSemester(PaymentStatus.COMPLETED,
// CURRENT_YEAR_SEMESTER);
//
//        List<Student> students = payments.stream()
//                .map(Payment::getStudent)
//                .toList();
//
//        List<Survey> surveys = surveyRepository.findByStudentIn(students);
//
//        List<ImportData> dataList = new ArrayList<>();
//
//        for (Payment payment : payments) {
//            Student student = payment.getStudent();
//            Member member = student.getMember();
//            Survey survey = surveys.stream()
//                    .filter(s -> s.getStudent().equals(student))
//                    .findFirst()
//                    .orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND));
//
//            ImportData data = new ImportData(
//                    payment.getUpdatedAt(),
//                    member.getName(),
//                    student.getStudentId(),
//                    student.getDepartment(),
//                    student.getGrade(),
//                    student.getSemester(),
//                    student.getAcademicStatus(),
//                    member.getPhoneNumber(),
//                    member.getDiscordId(),
//                    member.getEmail(),
//                    member.getBirthdate(),
//                    member.getGender(),
//                    student.getFresh(),
//                    survey.getInterests(),
//                    survey.getAcquisitionType(),
//                    survey.getJoinReason(),
//                    survey.getFeedback(),
//                    payment.getFinalPrice()
//            );
//
//            dataList.add(data);
//        }
//        // 가입일자(Member 생성 시간)를 기준으로 정렬
//        dataList.sort(Comparator.comparing(ImportData::joinDateTime));
//
//        // 2차원 배열로 데이터 준비
//        List<List<Object>> dataRows = dataList.stream()
//                .map(ImportData::toRowData)
//                .collect(Collectors.toList());
//
//        ValueRange body = new ValueRange()
//                .setValues(dataRows);
//
//        sheets.spreadsheets().values()
//                .append(spreadsheetId, REGISTRATION_SHEET_RANGE, body)
//                .setValueInputOption("RAW")
//                .setInsertDataOption("INSERT_ROWS")
//                .execute();
//    }
// }
