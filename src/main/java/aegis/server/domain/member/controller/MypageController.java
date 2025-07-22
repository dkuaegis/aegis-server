package aegis.server.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.member.dto.response.MypageResponse;
import aegis.server.domain.member.service.MypageService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Mypage", description = "마이페이지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;

    @Operation(summary = "마이페이지 조회", description = "사용자의 기본 정보(이름, 프로필 아이콘, 포인트 잔액)를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "마이페이지 정보 조회 성공")
    @GetMapping
    public ResponseEntity<MypageResponse> getMypage(@LoginUser UserDetails userDetails) {
        MypageResponse response = mypageService.getMypageSummary(userDetails);
        return ResponseEntity.ok(response);
    }
}
