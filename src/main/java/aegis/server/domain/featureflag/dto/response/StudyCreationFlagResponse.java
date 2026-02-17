package aegis.server.domain.featureflag.dto.response;

public record StudyCreationFlagResponse(
        Long featureFlagId, String rawValue, Boolean enabled, boolean valid, boolean studyCreationAllowed) {

    public static StudyCreationFlagResponse of(
            Long featureFlagId, String rawValue, Boolean enabled, boolean valid, boolean studyCreationAllowed) {
        return new StudyCreationFlagResponse(featureFlagId, rawValue, enabled, valid, studyCreationAllowed);
    }
}
