package aegis.server.domain.study.dto.response;

public record InstructorStudyApplicationReason(String applicationReason) {
    public static InstructorStudyApplicationReason from(String applicationReason) {
        return new InstructorStudyApplicationReason(applicationReason);
    }
}
