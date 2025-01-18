package aegis.server.domain.discord.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "discord_verification", timeToLive = 300)
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordVerification {

    @Id
    private String code;

    private Long memberId;

    public static DiscordVerification of(String code, Long memberId) {
        return new DiscordVerification(code, memberId);
    }
}
