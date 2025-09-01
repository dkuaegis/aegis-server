package aegis.server.domain.discord.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.discord.service.AdminDiscordService;

@Tag(name = "Admin Discord", description = "관리자 디스코드 권한 관리 API")
@RestController
@RequestMapping("/admin/discord")
@RequiredArgsConstructor
public class AdminDiscordController {

    private final AdminDiscordService adminDiscordService;

    @Operation(
            summary = "디스코드 역할 강등",
            description = "현재 학기 회비 미납 회원에게 부여된 디스코드 `@회원` 역할을 제거합니다. 관리자(ROLE_ADMIN)는 강등 대상에서 제외됩니다.",
            responses = {
                @ApiResponse(responseCode = "202", description = "요청 접수(비동기 처리)"),
                @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content),
                @ApiResponse(responseCode = "500", description = "디스코드 길드/역할/채널 설정 오류", content = @Content)
            })
    @PostMapping("/demote")
    public ResponseEntity<Void> demoteDiscordRolesForCurrentSemester() {
        adminDiscordService.demoteDiscordRolesForCurrentSemester();
        return ResponseEntity.accepted().build();
    }
}
