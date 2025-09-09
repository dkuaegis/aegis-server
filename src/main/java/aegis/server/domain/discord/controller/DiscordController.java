package aegis.server.domain.discord.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import aegis.server.domain.discord.dto.response.DiscordIdResponse;
import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.service.DiscordService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;

@Tag(name = "Discord", description = "디스코드 연동 관리 API")
@Slf4j
@RestController
@RequestMapping("/discord")
@RequiredArgsConstructor
public class DiscordController {

    private final DiscordService discordService;

    @Operation(
            summary = "내 디스코드 ID 조회",
            description = "로그인한 사용자의 연동된 디스코드 ID를 조회합니다. 현재 디스코드 서버 미가입(탈퇴 포함)인 경우 null을 반환합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "디스코드 ID 조회 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "디스코드 연동 정보찾을 수 없음", content = @Content)
            })
    @GetMapping("/myid")
    public ResponseEntity<DiscordIdResponse> getDiscordId(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        return ResponseEntity.ok(discordService.getDiscordId(userDetails));
    }

    @Operation(
            summary = "디스코드 인증 코드 발급",
            description = "디스코드 연동을 위한 인증 코드를 발급합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "인증 코드 발급 성공"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
                @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content),
                @ApiResponse(responseCode = "500", description = "인증 코드 생성 실패 (100회 시도 후 실패)", content = @Content)
            })
    @PostMapping("/issue-verification-code")
    public ResponseEntity<DiscordVerificationCodeResponse> getVerificationCode(
            @Parameter(hidden = true) @LoginUser UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discordService.createVerificationCode(userDetails));
    }
}
