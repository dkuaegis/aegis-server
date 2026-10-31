package aegis.server.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record InstructorStudyApplicationReason(
        @Schema(nullable = true) String applicationReason) {
    public static InstructorStudyApplicationReason from(String applicationReason) {
        return new InstructorStudyApplicationReason(applicationReason);
    }
}
