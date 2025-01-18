package aegis.server.domain.discord.controller;

import aegis.server.domain.discord.dto.response.DiscordVerificationCodeResponse;
import aegis.server.domain.discord.service.DiscordService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/discord")
@RequiredArgsConstructor
public class DiscordController {

    private final DiscordService discordService;

    @PostMapping("/issue-verification-code")
    public ResponseEntity<DiscordVerificationCodeResponse> getVerificationCode(@LoginUser SessionUser sessionUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(discordService.createVerificationCode(sessionUser));
    }
}
