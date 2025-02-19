package aegis.server.domain.discord.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(value = "discord_verification", timeToLive = 300)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordVerification {

    @Id
    private String code;

    @Indexed
    private Long memberId;

    public static DiscordVerification of(String code, Long memberId) {
        return new DiscordVerification(code, memberId);
    }
}
