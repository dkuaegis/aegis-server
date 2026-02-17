package aegis.server.domain.featureflag.dto.response;

public record AdminFeatureFlagsResponse(StudyEnrollWindowFlagResponse studyEnrollWindow) {

    public static AdminFeatureFlagsResponse of(StudyEnrollWindowFlagResponse studyEnrollWindow) {
        return new AdminFeatureFlagsResponse(studyEnrollWindow);
    }
}
