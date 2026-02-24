package aegis.server.domain.activity.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.dto.request.ActivityCreateUpdateRequest;
import aegis.server.domain.activity.dto.response.ActivityResponse;
import aegis.server.domain.activity.dto.response.AdminActivityPageResponse;
import aegis.server.domain.activity.service.ActivityService;

@Tag(name = "Admin Activity", description = "관리자 활동 관리 API")
@Validated
@RestController
@RequestMapping("/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    @Operation(
            summary = "모든 활동 조회",
            description = "관리자가 활동 목록을 검색/정렬/페이지네이션으로 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "활동 조회 성공")})
    @GetMapping
    public ResponseEntity<AdminActivityPageResponse> getAllActivities(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        AdminActivityPageResponse response = activityService.searchActivitiesForAdmin(page, size, keyword, sort);
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "활동 생성",
            description = "새로운 활동을 생성합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "활동 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "409", description = "동일한 이름의 활동이 이미 존재", content = @Content)
            })
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@Valid @RequestBody ActivityCreateUpdateRequest request) {
        ActivityResponse response = activityService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "활동 수정",
            description = "지정된 ID의 활동을 수정합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "활동 수정 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "404", description = "존재하지 않는 활동", content = @Content),
                @ApiResponse(responseCode = "409", description = "동일한 이름의 활동이 이미 존재", content = @Content)
            })
    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @Parameter(description = "수정할 활동 ID") @PathVariable Long activityId,
            @Valid @RequestBody ActivityCreateUpdateRequest request) {
        ActivityResponse response = activityService.updateActivity(activityId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "활동 삭제",
            description = "지정된 ID의 활동을 삭제합니다.",
            responses = {
                @ApiResponse(responseCode = "204", description = "활동 삭제 성공"),
                @ApiResponse(responseCode = "404", description = "존재하지 않는 활동", content = @Content),
                @ApiResponse(responseCode = "409", description = "참여 기록이 있어 삭제할 수 없음", content = @Content)
            })
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@Parameter(description = "삭제할 활동 ID") @PathVariable Long activityId) {
        activityService.deleteActivity(activityId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
