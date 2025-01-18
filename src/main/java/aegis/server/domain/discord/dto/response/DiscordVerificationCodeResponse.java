package aegis.server.domain.discord.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordVerificationCodeResponse {
    private String code;

    public static DiscordVerificationCodeResponse of(String code) {
        return new DiscordVerificationCodeResponse(code);
    }
}
