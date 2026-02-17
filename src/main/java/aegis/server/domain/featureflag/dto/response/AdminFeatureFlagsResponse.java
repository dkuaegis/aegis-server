package aegis.server.domain.featureflag.dto.response;

public record AdminFeatureFlagsResponse(
        StudyEnrollWindowFlagResponse studyEnrollWindow,
        MemberSignupFlagResponse memberSignup,
        StudyCreationFlagResponse studyCreation) {

    public static AdminFeatureFlagsResponse of(
            StudyEnrollWindowFlagResponse studyEnrollWindow,
            MemberSignupFlagResponse memberSignup,
            StudyCreationFlagResponse studyCreation) {
        return new AdminFeatureFlagsResponse(studyEnrollWindow, memberSignup, studyCreation);
    }
}
