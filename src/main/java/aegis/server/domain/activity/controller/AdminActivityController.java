package aegis.server.domain.activity.controller;

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

import aegis.server.domain.activity.dto.request.ActivityCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityResponse;
import aegis.server.domain.activity.service.ActivityService;

@Tag(name = "Admin Activity", description = "관리자 활동 관리 API")
@RestController
@RequestMapping("/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController {

    private final ActivityService activityService;

    @Operation(
            summary = "모든 활동 조회",
            description = "관리자가 등록된 모든 활동을 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "활동 조회 성공")})
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getAllActivities() {
        List<ActivityResponse> response = activityService.findAllActivities();
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
    public ResponseEntity<Void> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        activityService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "활동 활성화",
            description = "지정된 ID의 활동을 활성화합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "활동 활성화 성공"),
                @ApiResponse(responseCode = "404", description = "존재하지 않는 활동", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 활성화된 활동", content = @Content)
            })
    @PutMapping("/{activityId}/activate")
    public ResponseEntity<Void> activateActivity(@Parameter(description = "활성화할 활동 ID") @PathVariable Long activityId) {
        activityService.activateActivity(activityId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "활동 비활성화",
            description = "지정된 ID의 활동을 비활성화합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "활동 비활성화 성공"),
                @ApiResponse(responseCode = "404", description = "존재하지 않는 활동", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 비활성화된 활동", content = @Content)
            })
    @PutMapping("/{activityId}/deactivate")
    public ResponseEntity<Void> deactivateActivity(
            @Parameter(description = "비활성화할 활동 ID") @PathVariable Long activityId) {
        activityService.deactivateActivity(activityId);
        return ResponseEntity.ok().build();
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
