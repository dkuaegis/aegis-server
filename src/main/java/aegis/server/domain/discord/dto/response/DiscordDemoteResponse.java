package aegis.server.domain.discord.dto.response;

import java.util.List;

public record DiscordDemoteResponse(List<String> demotedMemberStudentIds) {
    public static DiscordDemoteResponse of(List<String> demotedMemberStudentIds) {
        return new DiscordDemoteResponse(demotedMemberStudentIds);
    }
}
