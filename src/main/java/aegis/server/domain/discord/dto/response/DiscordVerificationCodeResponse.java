package aegis.server.domain.discord.dto.response;

public record DiscordVerificationCodeResponse(String code) {

    public static DiscordVerificationCodeResponse of(String code) {
        return new DiscordVerificationCodeResponse(code);
    }
}
