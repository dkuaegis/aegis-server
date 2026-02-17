package aegis.server.domain.featureflag.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.dto.request.StudyEnrollWindowUpdateRequest;
import aegis.server.domain.featureflag.dto.response.AdminFeatureFlagsResponse;
import aegis.server.domain.featureflag.service.AdminFeatureFlagService;

@Tag(name = "Admin Feature Flag", description = "관리자 운영 플래그 관리 API")
@RestController
@RequestMapping("/admin/feature-flags")
@RequiredArgsConstructor
public class AdminFeatureFlagController {

    private final AdminFeatureFlagService adminFeatureFlagService;

    @Operation(
            summary = "운영 플래그 조회",
            description = "관리자가 현재 운영 플래그 값을 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "운영 플래그 조회 성공"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @GetMapping
    public ResponseEntity<AdminFeatureFlagsResponse> getFeatureFlags() {
        AdminFeatureFlagsResponse response = adminFeatureFlagService.getFeatureFlags();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "스터디 신청 기간 플래그 수정",
            description = "스터디 신청 허용 시작/종료 일시를 수정합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "스터디 신청 기간 수정 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content)
            })
    @PutMapping("/study-enroll-window")
    public ResponseEntity<AdminFeatureFlagsResponse> updateStudyEnrollWindow(
            @Valid @RequestBody StudyEnrollWindowUpdateRequest request) {
        AdminFeatureFlagsResponse response = adminFeatureFlagService.updateStudyEnrollWindow(request);
        return ResponseEntity.ok(response);
    }
}
