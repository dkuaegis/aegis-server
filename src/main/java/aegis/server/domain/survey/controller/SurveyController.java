package aegis.server.domain.survey.controller;

import aegis.server.domain.survey.dto.SurveyRequest;
import aegis.server.domain.survey.service.SurveyService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping("/{memberId}")
    @Operation(
            summary = "멤버 ID로 설문조사 조회",
            description = "DB에서 특정 회원의 설문 조사 조회")
    @ApiResponse(
            responseCode = "200", description = "설문조사 조회 성공",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SurveyRequest.class)))
    @ApiResponse(responseCode = "404", description = "설문조사 조회 실패")
    public ResponseEntity<SurveyRequest> getSurvey(@PathVariable Long memberId) {
        SurveyRequest surveyRequest = surveyService.findByMemberId(memberId);
        return new ResponseEntity<>(surveyRequest, HttpStatus.OK);
    }

    @PostMapping
    @Operation(
            summary = "설문 조사 생성",
            description = "설문 조사 양식에 맞는지 검증한 후, DB에 저장"
    )
    @ApiResponse(responseCode = "201",description = "설문조사 생성 성공")
    @ApiResponse(responseCode = "400", description = "설문조사 생성 실패 - 잘못된 요청 데이터")
    public ResponseEntity<SurveyRequest> createSurvey(
            @Validated @RequestBody SurveyRequest surveyRequest,
            @LoginUser SessionUser sessionUser) {
        surveyService.save(surveyRequest, sessionUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @Operation(
            summary = "모든 설문 조사 조회",
            description = "DB에 저장된 모든 설문 내역 반환"
    )
    @ApiResponse(responseCode = "200",description = "모든 설문 조사 내역 반환 성공",
    content = @Content(mediaType = "application/json",
    schema = @Schema(implementation = SurveyRequest.class)))
    public ResponseEntity<List<SurveyRequest>> getAllSurveys() {
        List<SurveyRequest> allSurveys = surveyService.getAllSurveys();
        return new ResponseEntity<>(allSurveys, HttpStatus.OK);
    }
}
