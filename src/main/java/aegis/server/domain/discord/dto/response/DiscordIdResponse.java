package aegis.server.domain.discord.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordIdResponse {

    private String discordId;

    public static DiscordIdResponse of(String discordId) {
        return new DiscordIdResponse(discordId);
    }
}
