package aegis.server.domain.point.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.point.dto.response.PointRankingListResponse;
import aegis.server.domain.point.dto.response.PointSummaryResponse;
import aegis.server.domain.point.service.PointService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Point", description = "포인트 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    private final PointService pointService;

    @Operation(
            summary = "포인트 요약 조회",
            description = "로그인한 사용자의 포인트 요약 정보를 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "포인트 요약 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/summary")
    public ResponseEntity<PointSummaryResponse> getPointSummary(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        PointSummaryResponse pointSummary = pointService.getPointSummary(userDetails);
        return ResponseEntity.ok(pointSummary);
    }

    @Operation(
            summary = "포인트 랭킹 조회",
            description =
                    "현재 학기 기준 포인트 적립량 상위 10명과 로그인한 사용자의 랭킹 정보를 조회합니다. STAFF(Role_STAFF)는 집계에서 제외되며, 이 경우 'me'가 null일 수 있습니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "포인트 랭킹 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
            })
    @GetMapping("/ranking")
    public ResponseEntity<PointRankingListResponse> getPointRanking(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        PointRankingListResponse pointRanking = pointService.getPointRanking(userDetails);
        return ResponseEntity.ok(pointRanking);
    }
}
