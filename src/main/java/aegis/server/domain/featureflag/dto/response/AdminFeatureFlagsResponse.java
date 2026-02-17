package aegis.server.domain.featureflag.dto.response;

public record AdminFeatureFlagsResponse(
        MemberSignupFlagResponse memberSignup,
        StudyCreationFlagResponse studyCreation,
        StudyEnrollWindowFlagResponse studyEnrollWindow) {

    public static AdminFeatureFlagsResponse of(
            MemberSignupFlagResponse memberSignup,
            StudyCreationFlagResponse studyCreation,
            StudyEnrollWindowFlagResponse studyEnrollWindow) {
        return new AdminFeatureFlagsResponse(memberSignup, studyCreation, studyEnrollWindow);
    }
}
