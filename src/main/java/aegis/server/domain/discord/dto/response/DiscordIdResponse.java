package aegis.server.domain.discord.dto.response;

public record DiscordIdResponse(String discordId) {

    public static DiscordIdResponse of(String discordId) {
        return new DiscordIdResponse(discordId);
    }
}
