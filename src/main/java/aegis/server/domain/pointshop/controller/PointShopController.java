package aegis.server.domain.pointshop.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.pointshop.dto.response.PointShopDrawHistoryResponse;
import aegis.server.domain.pointshop.dto.response.PointShopDrawResponse;
import aegis.server.domain.pointshop.service.PointShopService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Point Shop", description = "포인트 샵 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/point-shop")
public class PointShopController {

    private final PointShopService pointShopService;

    @Operation(
            summary = "내 뽑기 기록 조회",
            description = "로그인한 사용자의 포인트 샵 뽑기 이력을 최신순으로 조회합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
            })
    @GetMapping("/draws/me")
    public ResponseEntity<List<PointShopDrawHistoryResponse>> getMyDrawHistories(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        List<PointShopDrawHistoryResponse> responses = pointShopService.getMyDrawHistories(userDetails);
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "포인트 샵 뽑기",
            description = "포인트 잔액을 차감하고 가중치에 따라 상품을 뽑습니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "뽑기 성공"),
                @ApiResponse(responseCode = "400", description = "잔액 부족", content = @Content),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "포인트 계좌 없음", content = @Content)
            })
    @PostMapping("/draw")
    public ResponseEntity<PointShopDrawResponse> draw(@Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        PointShopDrawResponse response = pointShopService.draw(userDetails);
        return ResponseEntity.ok(response);
    }
}
