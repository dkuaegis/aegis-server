package aegis.server.domain.study.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.study.dto.request.StudyCreateUpdateRequest;
import aegis.server.domain.study.dto.response.AttendanceCodeIssueResponse;
import aegis.server.domain.study.dto.response.AttendanceMatrixResponse;
import aegis.server.domain.study.dto.response.GeneralStudyDetail;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationReason;
import aegis.server.domain.study.dto.response.InstructorStudyApplicationSummary;
import aegis.server.domain.study.dto.response.InstructorStudyMemberResponse;
import aegis.server.domain.study.service.StudyInstructorService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Study Instructor", description = "스터디장용 API")
@RestController
@RequiredArgsConstructor
public class StudyInstructorController {

    private final StudyInstructorService studyInstructorService;

    @Operation(
            summary = "스터디 지원서 목록 조회",
            description = "스터디장 또는 관리자가 스터디의 모든 지원서 목록을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원서 목록 조회 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}/applications-instructor")
    public ResponseEntity<List<InstructorStudyApplicationSummary>> getStudyApplications(
            @PathVariable Long studyId, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        List<InstructorStudyApplicationSummary> response =
                studyInstructorService.findAllStudyApplications(studyId, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디원 목록 조회",
            description = "스터디장 또는 관리자가 스터디의 스터디원 목록을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "스터디원 목록 조회 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}/members-instructor")
    public ResponseEntity<List<InstructorStudyMemberResponse>> getStudyMembers(
            @PathVariable Long studyId, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        List<InstructorStudyMemberResponse> response = studyInstructorService.findAllStudyMembers(studyId, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 지원서 상세 조회",
            description = "스터디장 또는 관리자가 특정 지원서의 상세 내용을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원서 상세 조회 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}/applications/{studyApplicationId}")
    public ResponseEntity<InstructorStudyApplicationReason> findStudyApplicationById(
            @PathVariable Long studyId,
            @PathVariable Long studyApplicationId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        InstructorStudyApplicationReason response =
                studyInstructorService.findStudyApplicationById(studyId, studyApplicationId, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 정보 수정",
            description = "스터디장 또는 관리자가 스터디 정보를 수정합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "스터디 수정 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @PutMapping("/studies/{studyId}")
    public ResponseEntity<GeneralStudyDetail> updateStudy(
            @PathVariable Long studyId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails,
            @Valid @RequestBody StudyCreateUpdateRequest request) {
        GeneralStudyDetail response = studyInstructorService.updateStudy(studyId, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 지원서 승인",
            description = "스터디장 또는 관리자가 지원서를 승인하여 지원자를 스터디 멤버로 추가합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원서 승인 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음", content = @Content)
            })
    @PutMapping("/studies/{studyId}/applications/{studyApplicationId}/approve")
    public ResponseEntity<Void> approveStudyApplication(
            @PathVariable Long studyId,
            @PathVariable Long studyApplicationId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        studyInstructorService.approveStudyApplication(studyId, studyApplicationId, userDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "스터디 지원서 거절",
            description = "스터디장 또는 관리자가 지원서를 거절합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "지원서 거절 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "지원서를 찾을 수 없음", content = @Content)
            })
    @PutMapping("/studies/{studyId}/applications/{studyApplicationId}/reject")
    public ResponseEntity<Void> rejectStudyApplication(
            @PathVariable Long studyId,
            @PathVariable Long studyApplicationId,
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        studyInstructorService.rejectStudyApplication(studyId, studyApplicationId, userDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "출석 코드 발급",
            description = "스터디장 또는 관리자가 오늘 날짜의 세션에 대한 출석 코드를 발급합니다. 같은 날 재발급 시 동일 코드를 반환합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "출석 코드 발급 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @PostMapping("/studies/{studyId}/attendance-code")
    public ResponseEntity<AttendanceCodeIssueResponse> issueAttendanceCode(
            @PathVariable Long studyId, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        AttendanceCodeIssueResponse response = studyInstructorService.issueAttendanceCode(studyId, userDetails);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(
            summary = "회차별 출석 현황 조회",
            description = "스터디장 또는 관리자가 이름 기준 행, 회차 기준 열의 매트릭스를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "출석 현황 조회 성공"),
                @ApiResponse(responseCode = "403", description = "스터디장/관리자 권한 아님", content = @Content),
                @ApiResponse(responseCode = "404", description = "스터디를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/studies/{studyId}/attendance-instructor")
    public ResponseEntity<AttendanceMatrixResponse> getAttendanceMatrix(
            @PathVariable Long studyId, @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        AttendanceMatrixResponse response = studyInstructorService.findAttendanceMatrix(studyId, userDetails);
        return ResponseEntity.ok(response);
    }
}
