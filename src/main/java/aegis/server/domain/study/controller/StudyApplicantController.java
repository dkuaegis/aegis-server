package aegis.server.domain.study.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.dto.request.StudyEnrollRequest;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationDetail;
import aegis.server.domain.study.dto.response.ApplicantStudyApplicationStatus;
import aegis.server.domain.study.service.StudyApplicantService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Study Applicant", description = "스터디 지원서 제출자가 사용하는 API / 선착순 스터디에 대해서는 지원하지 않음")
@RestController
@RequiredArgsConstructor
public class StudyApplicantController {

    private final StudyApplicantService studyApplicantService;

    @Operation(
            summary = "스터디 지원 상태 조회",
            description = "지원서 스터디에 대해 본인의 지원 상태를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원 상태 조회 성공"),
                @ApiResponse(responseCode = "404", description = "스터디 또는 지원서를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}/status")
    public ResponseEntity<ApplicantStudyApplicationStatus> getStudyApplicationStatus(
            @PathVariable Long studyId, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        ApplicantStudyApplicationStatus response =
                studyApplicantService.getStudyApplicationStatus(studyId, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 지원서 상세 조회",
            description = "본인이 제출한 스터디 지원서의 상세 내용을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원서 상세 조회 성공"),
                @ApiResponse(responseCode = "404", description = "스터디 또는 지원서를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}/applications")
    public ResponseEntity<ApplicantStudyApplicationDetail> getStudyApplicationDetail(
            @PathVariable Long studyId, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        ApplicantStudyApplicationDetail response =
                studyApplicantService.getStudyApplicationDetail(studyId, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 지원서 수정",
            description = "본인이 제출한 스터디 지원서를 수정합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원서 수정 성공"),
                @ApiResponse(responseCode = "404", description = "스터디 또는 지원서를 찾을 수 없음", content = @Content)
            })
    @PutMapping("/studies/{studyId}/applications")
    public ResponseEntity<ApplicantStudyApplicationDetail> updateStudyApplicationReason(
            @PathVariable Long studyId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @RequestBody StudyEnrollRequest request) {
        ApplicantStudyApplicationDetail response =
                studyApplicantService.updateStudyApplicationReason(studyId, request, userDetails);
        return ResponseEntity.ok(response);
    }
}
