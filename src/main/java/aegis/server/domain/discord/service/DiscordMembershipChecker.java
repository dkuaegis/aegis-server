package aegis.server.domain.discord.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMembershipChecker {

    private final JDA jda;

    @Value("${discord.guild-id}")
    private String guildId;

    public boolean isMember(String discordId) {
        if (discordId == null || discordId.isBlank()) {
            return false;
        }

        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            log.error("[DiscordMembershipChecker] Guild not found: guildId={}", guildId);
            // 길드 조회 불가 시 기존 동작 보존을 위해 가입으로 간주
            return true;
        }

        try {
            CompletableFuture<Member> future =
                    guild.retrieveMemberById(discordId).submit();
            Member member = future.get(3, TimeUnit.SECONDS);
            return member != null;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ErrorResponseException ere) {
                ErrorResponse er = ere.getErrorResponse();
                if (er == ErrorResponse.UNKNOWN_MEMBER || er == ErrorResponse.UNKNOWN_USER) {
                    // 서버에 없는 사용자
                    return false;
                }
                log.error("[DiscordMembershipChecker] Discord API error: {}", er);
                return true; // 기타 오류는 보수적으로 가입으로 간주
            }
            if (cause != null) {
                log.error("[DiscordMembershipChecker] Execution error: {}", cause, cause);
            } else {
                log.error("[DiscordMembershipChecker] Execution error", e);
            }
            return true;
        } catch (TimeoutException e) {
            log.error("[DiscordMembershipChecker] Timeout while retrieving member: discordId={}", discordId);
            return true; // 타임아웃 시 보수적으로 가입으로 간주
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[DiscordMembershipChecker] Interrupted while retrieving member: discordId={}", discordId);
            return true;
        } catch (IllegalArgumentException e) {
            // 잘못된 디스코드 ID 형식
            log.info("[DiscordMembershipChecker] Invalid discordId format: {}", discordId);
            return false;
        }
    }
}
