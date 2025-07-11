package aegis.server.domain.member.dto.response;

import java.util.List;

public record MemberDemoteResponse(List<String> demotedMemberStudentIds) {

    public static MemberDemoteResponse of(List<String> demotedMemberStudentIds) {
        return new MemberDemoteResponse(demotedMemberStudentIds);
    }
}
