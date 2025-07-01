package aegis.server.domain.survey.controller;

import aegis.server.domain.survey.dto.SurveyCommon;
import aegis.server.domain.survey.service.SurveyService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Survey", description = "설문조사 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "내 설문조사 조회", description = "로그인한 사용자의 설문조사 답변을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "설문조사 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "설문조사 답변을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<SurveyCommon> getSurvey(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails
    ) {
        return ResponseEntity.ok(surveyService.getSurvey(userDetails));
    }

    @Operation(summary = "설문조사 작성/수정", description = "로그인한 사용자의 설문조사 답변을 작성하거나 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "설문조사 작성/수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    public ResponseEntity<Void> createOrUpdateSurvey(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody SurveyCommon request
    ) {
        surveyService.createOrUpdateSurvey(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
