package aegis.server.domain.featureflag.dto.response;

public record AdminFeatureFlagsResponse(
        StudyEnrollWindowFlagResponse studyEnrollWindow,
        MemberSignupWriteFlagResponse memberSignupWrite,
        StudyCreationFlagResponse studyCreation) {

    public static AdminFeatureFlagsResponse of(
            StudyEnrollWindowFlagResponse studyEnrollWindow,
            MemberSignupWriteFlagResponse memberSignupWrite,
            StudyCreationFlagResponse studyCreation) {
        return new AdminFeatureFlagsResponse(studyEnrollWindow, memberSignupWrite, studyCreation);
    }
}
