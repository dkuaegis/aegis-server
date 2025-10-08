package aegis.server.domain.study.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.GeneralStudyRolesIdsResponse;
import aegis.server.domain.study.dto.response.GeneralStudySummary;
import aegis.server.domain.study.service.StudyGeneralService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Study General", description = "일반 회원이 사용하는 스터디 API")
@RestController
@RequiredArgsConstructor
public class StudyGeneralController {

    private final StudyGeneralService studyGeneralService;

    @Operation(
            summary = "스터디 목록 조회",
            description = "모든 스터디의 목록을 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "스터디 목록 조회 성공")})
    @GetMapping("/studies")
    public ResponseEntity<List<GeneralStudySummary>> findAllStudies() {
        List<GeneralStudySummary> response = studyGeneralService.findAllStudies();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 상세 조회",
            description = "스터디의 상세 정보를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "스터디 상세 조회 성공"),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}")
    public ResponseEntity<GeneralStudyDetail> getStudyDetail(@PathVariable Long studyId) {
        GeneralStudyDetail response = studyGeneralService.getStudyDetail(studyId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 생성",
            description = "새로운 스터디를 생성합니다. 생성한 사용자는 자동으로 스터디장이 됩니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "스터디 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
            })
    @PostMapping("/studies")
    public ResponseEntity<GeneralStudyDetail> createStudy(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody StudyCreateUpdateRequest request) {
        GeneralStudyDetail response = studyGeneralService.createStudy(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "스터디 참여 신청",
            description = "스터디에 참여 신청을 합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "스터디 참여 신청 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "스터디 신청 차단됨", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @PostMapping("/studies/{studyId}/enrollment")
    public ResponseEntity<Void> enrollInStudy(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @PathVariable Long studyId,
            @RequestBody StudyEnrollRequest request) {
        studyGeneralService.enrollInStudy(studyId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "내 스터디 권한 조회",
            description = "본인이 스터디장/스터디원/지원자인 스터디 목록을 조회합니다. 관리자는 현재 학기의 모든 스터디를 스터디장 권한으로 반환합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/roles")
    public ResponseEntity<GeneralStudyRolesIdsResponse> getMyStudyRoles(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        GeneralStudyRolesIdsResponse response = studyGeneralService.getMyStudyRoles(userDetails);
        return ResponseEntity.ok(response);
    }
}
