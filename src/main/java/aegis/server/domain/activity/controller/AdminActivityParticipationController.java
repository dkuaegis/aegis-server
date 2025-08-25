package aegis.server.domain.activity.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.activity.dto.request.ActivityParticipationCreateRequest;
import aegis.server.domain.activity.dto.response.ActivityParticipationResponse;
import aegis.server.domain.activity.service.ActivityParticipationService;

@Tag(name = "Admin ActivityParticipation", description = "관리자 활동 참여 관리 API")
@RestController
@RequestMapping("/admin/activity-participation")
@RequiredArgsConstructor
public class AdminActivityParticipationController {

    private final ActivityParticipationService activityParticipationService;

    @Operation(
            summary = "활동 참여 생성",
            description = "관리자가 활동 참여를 생성합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "활동 참여 생성 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "404", description = "존재하지 않는 활동 또는 회원", content = @Content),
                @ApiResponse(responseCode = "409", description = "이미 존재하는 활동 참여", content = @Content)
            })
    @PostMapping
    public ResponseEntity<ActivityParticipationResponse> create(
            @Valid @RequestBody ActivityParticipationCreateRequest request) {
        ActivityParticipationResponse response = activityParticipationService.createActivityParticipation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
